# Architecture decisions

This document records the decisions taken while migrating the original Flutter
app (reference only; it has since been removed) to Compose Multiplatform.
Desktop is the only target wired up for now; Android compiles against the same
shared code.

## Goal

Keep the product behaviour and the layering of the Flutter app, but express it
with Kotlin Multiplatform best practices: pure domain, replaceable data
adapters, an isolated player, and a UI that never talks to HTTP, the filesystem
or the playback engine directly.

## Modules and dependency direction

```
:core      pure domain (models, repository contracts, watched rules, player contract)
:data      implementations (Stremio addons, Nuvio/Supabase, local JSON store)
:app:shared  Compose UI + ViewModels (depends only on :core)
:app:desktopApp  composition root: libmpv player, window control, main()
:app:androidApp  Android composition root (no-op player for now)
```

`ui -> domain` and `data -> domain` (dependency inversion): the data layer
implements the contracts declared in `:core`, and `:data` is only referenced by
the composition roots. Swapping a backend or the player changes one module and
the composition root, never the UI.

`:core` and `:data` are Kotlin Multiplatform (`commonMain` + `jvmMain` +
`androidMain`). Domain and data code lives in `commonMain`; only the HTTP
engine, the filesystem and the player touch platform source sets through
`expect`/`actual` (`data/platform/Platform.kt`).

## Domain (`:core`)

- **Models are plain data classes**, with no database-column mapping and no JSON
  parsing. Row parsing lives in `:data` (`NuvioMappers.kt`), so the models can be
  produced by any backend.
- **One repository per capability**, mirroring the Flutter refactor:
  `AccountRepository`, `LibraryRepository`, `ProgressRepository`,
  `AddonRepository`, plus the addon contracts (`CatalogRepository`,
  `MetadataRepository`, `StreamRepository`, `SubtitleRepository`) and
  `RatingRepository`.
- **Observability with coroutines.** Where Flutter used `Listenable`, the
  Kotlin contracts expose `StateFlow`. `AccountRepository` exposes a single
  `StateFlow<AccountState>` (session, profiles, active profile, loading and
  error); `LibraryRepository`/`ProgressRepository` expose a `StateFlow<Long>`
  change counter that screens collect to reload. This removes the
  `SessionController` patch entirely.
- **Profiles are optional** (`AccountState.requiresProfile`) and mandatory when
  required: there is no default profile, because a write saved to the wrong
  profile is wrong data, not missing data.
- **Watched logic is pure** (`watched/WatchedRule.kt`) and the resolver
  (`watched/WatchedBadges.kt`) keeps the bounded-concurrency, cached,
  background behaviour of the original.
- **Sign-in is a request, not a signature.** `AccountRepository.signIn` takes a
  `SignInRequest` (sealed; today only `EmailPassword`) and the backend declares
  what it offers through `signInMethods`, so the sign-in UI renders the matching
  form instead of assuming email + password. Adding OAuth or a magic link means
  a new subtype, not a contract change; a multi-step flow (device pairing) would
  still need a session object on top of this.
- **`PlayerService` is a contract**, with a neutral `PlayerState` and a
  `videoOutput: Any?` handle so the UI can embed the native surface without
  depending on the engine.

## Data (`:data`)

- **Ktor client** (CIO on desktop, OkHttp on Android) instead of `package:http`.
- **kotlinx.serialization** with the `JsonElement` API and small defensive
  coercion helpers (`JsonUtils.kt`), mirroring `json_utils.dart`: third-party
  payloads degrade to `null`/empty instead of throwing.
- **Stremio**: `StremioAddonClient` plus the four repositories. Same rules as
  the original: metadata is first-wins across addons, streams/subtitles are
  aggregated in parallel, and catalog `extra` values are encoded while keeping
  `=`/`&` raw. The "similar" row picks a catalog that can filter by genre and
  skips catalogs whose required extras cannot be provided (a search term, a list
  of ids), then falls back to the generic catalog, so a title is not left
  without the row.
- **Nuvio/Supabase**: raw HTTP split by responsibility, instead of one client
  class: `NuvioTransport` (verbs and base URL), `NuvioDiscovery`
  (`/.well-known/nuvio` and the publishable key), `NuvioAuth` (session, sign-in,
  refresh, sign-out) and `NuvioRest` (PostgREST select/insert/delete and the
  `sync_*` RPCs). Repositories take the slice they need. Session tokens live in
  `data/`, never in the domain.
- **Local store**: `JsonFileStore`/`JsonMapStore` and the session / ratings /
  watched-series stores. Path resolution is a platform `expect`
  (`%APPDATA%\Agustream` on Windows, XDG elsewhere). Writes go through a
  sibling temporary file and are renamed into place, so an interrupted write
  cannot leave a truncated document, and a failed write is reported instead of
  being swallowed. Read-modify-write cycles in the local repositories are
  serialized with a `Mutex`. The session file is restricted to the current user
  where the OS allows it; that is best-effort hardening, not a keychain.
- **Settings** live in `settings.json` as a flat JSON object with dotted keys,
  like VS Code's `settings.json` (`animations.enabled`, `player.resumePlayback`,
  `player.autoPlayNext`, `poster.size`, `library.poster.size`).
  `JsonSettingsRepository` reads it defensively, keeps unknown keys on write and
  can `reload()` after a manual edit; the settings screen groups the toggles by
  category, edits the same file, shows its path and can open it in the OS
  editor.
- **Session renewal**: `NuvioAuth` renews the token before a request when it
  has expired, and `NuvioRest` renews once more when the backend answers `401`.
  Renewals are serialized with a mutex so parallel requests do not refresh
  twice. Engines are configured with finite connect/request/socket timeouts.
- **`BackendKind` + `createBackend`** is the single place that knows the
  concrete implementations, replacing the Flutter `backend_factory.dart`.
- **Parallel I/O and caching.** Independent reads run together instead of in a
  row: Home loads watch progress, the library and the catalog list concurrently;
  Library loads the library and its progress together; the watched-badge
  resolver reads the two progress sources in parallel and its metadata lookups
  are bounded but concurrent; `watched_items` chunks are queried together;
  catalog manifests are fetched in parallel and cached per addon (a manifest is
  immutable). The addon list is cached per profile uuid, so the watched-badge
  sync does not re-query it for every series. Account-scoped ViewModels (Home,
  Library, Search (and its discover feed), Calendar) wait for a profile before
  loading rather than loading without one and failing.

## UI (`:app:shared`)

- **No DI framework.** `AppServices` is a data holder provided through
  `LocalAppServices`; the composition roots build it. This is the direct
  equivalent of the Flutter `AppServices` `InheritedWidget`.
- **ViewModel + `StateFlow`.** Each section/route has a `ViewModel` that owns
  its state and launches work in `viewModelScope`. The UI collects with
  `collectAsStateWithLifecycle()`, so collection stops when a screen leaves the
  lifecycle.
- **Compose lifecycle.** A root `ViewModelStoreOwner` is provided by
  `AgustreamApp`. Section ViewModels live in it and survive section switches
  (replacing Flutter's `IndexedStack` state retention). Pushed routes get their
  own `ViewModelStoreOwner` (`ContentArea`) that is cleared on pop, so transient
  ViewModels do not leak.
- **Onboarding and session bootstrap.** `AgustreamApp` resolves the persisted
  session once, before anything else: it shows a splash until that finishes, then
  the onboarding (no session) or the shell, so a returning user never sees the
  sign-in for a frame. It is a hard gate, so the sections no longer carry a
  "not signed in" state: signing out returns to the onboarding. The form is
  shared with the Settings account card and driven by `signInMethods`, so a
  backend that signs in another way renders another form. The desktop
  `NUVIO_EMAIL` / `NUVIO_PASSWORD` shortcut is passed in as an `autoSignIn`
  request, applied only when there is no stored session.
- **Adaptive layout.** `AppShell` measures the window once with
  `BoxWithConstraints` and publishes a `WindowSize` (compact / medium /
  expanded, at Material's 600 and 840 dp breakpoints) plus the window width,
  through `LocalWindowSize` / `LocalWindowWidth`, so a surface picks a layout
  without measuring anything itself. Compact windows (phones) always get an
  icon-only bottom bar and reserve its height; wider windows always get a left
  navigation rail and reserve its width.
  The poster grids and rails scale their cell width down on compact windows
  (the user's small / default / large choice still applies), the Home hero swaps
  the wide billboard for the poster crop without the title logo, the detail
  billboard is capped on an expanded window so the content stays close, and the
  streams dialog clamps to the app window because it opens its own. Compact
  windows also use a tighter screen padding (16dp) and grid gap, and the detail
  actions are icon-only (with a `contentDescription`) at the call-to-action's
  height, so the row lines up.
- **Edge-to-edge and system insets.** Android runs the activity edge-to-edge
  (`enableEdgeToEdge`, with light system-bar icons forced because the app always
  paints dark), and the `Scaffold` no longer reserves the system bars
  (`contentWindowInsets` is zero). Screens place the insets themselves through
  `safeTopInset` / `safeBottomInset` (`ui/theme/WindowSize.kt`, backed by
  `WindowInsets.statusBars` / `navigationBars`, zero on desktop) or the
  `statusBarsPadding` / `navigationBarsPadding` / `safeDrawingPadding`
  modifiers. The shell reserves the navigation plus the system bars it sits
  behind for a section (the bottom bar on a phone, the rail on a wide
  window); the navigation bars carry their own inset; the detail backdrop, the
  player and the onboarding/profile gates paint behind the bars while their
  controls sit below them; and the Home billboard scrolls with the content (it
  is never pinned). On desktop the complete artwork is drawn `Fit` (no crop, no
  letterbox bars) at 70% of the window width, pinned to the top-right corner of
  the frame, so the sections below start higher without losing image detail. It
  is washed by `BrandPalette.background` gradients: a left fade that dissolves
  the artwork's left edge under the copy, a bottom fade that melts into
  "Continue watching" without a seam, a subtle top fade, and a right-edge
  vignette. On a phone the billboard spans the full width, sized to the
  artwork's aspect ratio. The copy column is
  bottom-anchored 100dp above the hero edge, up to 840dp wide, and shares the
  sections' left padding. The synopsis is a teaser — capped tighter (520dp,
  about 60 characters), three lines at most, the rest behind "More info". On a
  wide window the rail floats over the content,
  which reserves its width, so the navigation never overlaps it.
- **Typography.** Inter is bundled through Compose Resources, so every target
  renders the same typeface, and text is written in `sp` so Android's system
  font size keeps working. `LocalDensity` is deliberately never overridden: that
  is the common hack, and it lies to every component about the font scale.
  Material's `display*` roles are sized for a big screen, so `AppType` steps
  them down one role on a compact window (hero title, screen titles, hero
  synopsis); desktop and tablet keep the full scale. No type slot carries a
  colour: text colour comes from the colour scheme at the call site, as Material
  intends, and `PosterCaption` is just the `titleSmall` / `bodySmall` roles.
- **Navigation** is a small typed stack (`ContentNavigator` + `ContentRoute`)
  rendered inside the shell content area. Sections keep the navigation
  visible; a pushed route (detail, catalog) hides it, so an immersive hero such
  as the detail one fills the whole window — the same behaviour as the Flutter
  nested `Navigator`, without adding a navigation library. The chrome mixes
  Material and custom: a compact window gets a `NavigationBar` with icon-only
  `NavigationBarItem`s; a wide window a left navigation rail with custom
  icon-only items. The rail drops the labels to stay narrow, centers the
  profile avatar and the section items in the window, and paints the page
  background instead of a panel colour, so it blends into the content; the
  selected item is a translucent on-surface fill, with the hover/press state
  layer blended over it so a selected item never loses its fill.
- **Search owns the discover feed.** It has a free-text search across every
  catalog that declares the `search` extra, debounced, run in parallel and
  deduped across addons, split into Movies / Series / Other rails; and — while
  the query is empty — the discover feed underneath the bar. The feed flattens
  every catalog the enabled addons declare into a single mixed grid, like the
  Library, so the catalog boundaries stay invisible. Only browsable catalogs are
  used: one whose extras are required (a search term, a list of ids, ...) cannot
  be fetched without a query, so it is skipped instead of asking and getting an
  empty page. The catalogs are interleaved across addons before the cap, so one
  addon with many catalogs does not crowd out the rest, and the items are
  interleaved round-robin and deduped, so the grid alternates sources instead of
  showing one catalog as a block. It pages with `skip` as the grid is scrolled,
  so it keeps growing instead of stopping at the first page, and it keeps paging
  while a narrow filter has not yet yielded enough items to show. Two dropdowns
  — the title type (All / Movies / Series) and the genre — narrow both the feed
  and the search results; the genre menu is built from the genres of the items
  that have loaded, so it never offers one the addons do not return. Discover is
  no longer a section. **Calendar** paints a month grid with the upcoming
  episodes of the Library's series; see the limitation below.
- **Theme** follows Material 3. `AgustreamTheme` supplies the full
  `darkColorScheme` (every role, including the surface-container tiers,
  `outline`/`outlineVariant`, `inverse*`, `tertiary` and the error containers),
  the Inter typeface loaded from Compose Resources, and a small, square-ish
  shape scale (`AppCorner` feeds `MaterialTheme.shapes`: 2/4/6/8/12dp; the app
  reads square, not rounded). The UI reads
  semantic colours from `MaterialTheme.colorScheme`; the raw brand palette is
  private and only fills the scheme, so stock Material3 components and custom
  surfaces stay on the same colours. `AppColors` keeps only `mediaScrim`, a
  media overlay with no scheme role, and `AppSizes.controlHeight` /
  `minTouchTarget` keep interactive targets at Material's 48dp minimum. Custom
  clickable surfaces draw the Material ripple state layer (`clickableSurface`),
  so focus and press feedback stays visible, and carry a button role so a custom
  surface is announced correctly. Poster tiles opt out of that state layer
  (`indication = null`) and signal hover with the scale/border animation
  instead, so a hovered poster is not greyed. Settings panels use the Material
  `Card`, and the primary call to action uses the inverse surface / on-surface
  roles. The near-black page background (`#0B0803`) is the single source of
  truth: the scheme, the native window background and every page-fade gradient
  end on it. The
  app is deliberately **dark-only** — no light scheme and no dynamic colour —
  because the UI is built around dark artwork and scrims; the player keeps a
  fixed white-on-black control palette over video. Material3 `visualDensity`
  stays at the platform default.
- **Images with Coil 3**, the multiplatform image loader. Remote artwork goes
  through two shared composables in `ui/components`: `PosterImage` for posters
  and thumbnails, and `RemoteImage` for the rest (backdrops, hero art, cast
  photos, title logos). Both pin `ContentScale` and `FilterQuality`, because
  Coil draws with `DefaultFilterQuality` (bilinear) and aliases a downscaled
  poster. Artwork uses `FilterQuality.Medium` (mipmapped bilinear), which is the
  right sampling for minification, and logos use `FilterQuality.High` (bicubic)
  so their edges stay crisp. On desktop the decode side is fixed too: Coil
  3.6.3's `SkiaImageDecoder` resizes with `SamplingMode.DEFAULT`, which Skiko
  defines as nearest-neighbour, so `app/desktopApp` installs
  `MitchellImageDecoder`, a cubic decoder that keeps Coil's size math and
  reduces once at decode time. Together they both sharpen the artwork and cut
  the bitmap memory the renderer has to scale every frame. Title logos are
  contained in a box — a fixed optical height plus a `maxWidth` guard
  (`AppLogoSize`), drawn with `ContentScale.Fit` — so a square logo cannot
  tower over the copy and a very wide one cannot overflow its column. The Home
  hero preloads every featured backdrop and logo through the singleton loader,
  sized to the billboard, so swiping the carousel paints from cache instead of
  starting a network round-trip mid-gesture.
- **Motion** is centralised in `ui/theme/Motion.kt`: `AppMotion` holds the
  durations and both the utility and the Material emphasized easings (entering
  screens use emphasized decelerate, exiting ones emphasized accelerate), and
  `LocalMotionEnabled` gates every animation. Specs
  built through `AppMotion.spec`/`motionTween` collapse to zero duration when it
  is off, and `Modifier.motionLayer` applies scale and alpha in the draw phase,
  so motion never relayouts or recomposes per frame. Tabs swap with no
  transition; the section
  stays composed while a route is open (so its scroll is never saved and
  restored, which lost the within-item offset and scrolled it back up) and the
  route fades in over it, leaving at once when popped. The rail floats over
  the content instead of taking layout width, because hiding it used to grow
  the content viewport and clamp the section scroll; sections reserve its width
  with a constant padding, so the viewport never changes. The size animation is
  disabled (`.using(null)`) because it relayouts every frame. Posters scale on
  hover and dialogs fade/scale in. Loading screens are skeletons driven by a
  single shimmer read in the draw phase: the poster grid derives its cell count
  from the viewport, so it never stops halfway down a wide window, and the
  static gradient stops are built once instead of on every frame. Home has no
  skeleton on purpose: it paints nothing until the data arrives, instead of
  flashing a billboard placeholder that then changes shape. The detail hero does not paint the library fallback as if it were the final
  artwork: it reserves the logo's space without drawing a placeholder, and the
  logo and backdrop fade in when the image itself is ready (`RemoteImage(fadeIn)`),
  so a slow load never pops. The player is deliberately outside the route
  transition because it drives a single shared engine.
- **Poster sizes** are a setting (`PosterSize`: small / default / large, 210 /
  230 / 250 dp). `poster.size` is shared by Home, the discover feed, the catalog
  grids and "More like this", while `library.poster.size` applies only to the
  Library grid. A grid uses the size as its minimum cell width and a rail derives its
  height from it (`width / 0.58`), so the card aspect is the same everywhere and
  every surface can be resized from a single key.
- **Window**: the app keeps the **native window decoration** (title bar,
  minimize, maximize, close and resizing are OS-managed), which avoids the
  glitches a custom title bar can cause on Windows. Compose Desktop draws its
  own resizers only when the window is undecorated, so nothing custom is needed.
  The window opens at 1280x720 and that is also its **minimum** (set as
  `minimumSize` on the AWT window), so the OS lets it grow and maximise but
  never shrink below the size it started with.
  The only window behaviour routed through `WindowController` is **fullscreen**,
  which the player owns; the previous placement is restored on exit. On Windows
  the native title bar is opted into the DWM immersive dark mode and, on
  Windows 11, tinted to the window chrome colour (`AppWindowChrome.titleBar`,
  `WindowAppearance.kt`), because AWT does not follow the OS dark mode on its
  own.

## Player

`PlayerService` is implemented on desktop by `MpvPlayerService`, which loads
**libmpv** in process through JNA, in the composition root so the engine stays
out of the shared UI. Video is produced with libmpv's **render API** (software
backend) and handed to the UI as Compose bitmaps through `FrameSource`, so the
video is ordinary Compose content and the Compose controls stay on top of it; a
native `wid` surface would cover them. The rest (commands, position, tracks)
goes through the libmpv client API. On Windows the `libmpv-2.dll` is bundled
with the app; on Linux the system `libmpv.so` is used. The library is loaded
lazily, so a machine without it degrades to a clear playback error. Watch
progress reporting, resume, subtitles/audio selection and next-episode autoplay
are ported from the original. The software renderer is CPU-bound; moving to the
OpenGL render backend is the planned performance step.

## Testing

`commonTest` covers the pure domain (playback target, watched rules, year
parsing, badge resolver with fakes) and the defensive parsers/mappers.
`jvmTest` covers the file stores and the local repositories. `:app:shared`
`commonTest` covers the section ViewModels with in-memory repositories and
`Dispatchers.setMain`.

## Known differences / limitations

- The desktop player needs libmpv: on Windows it is bundled (`libmpv-2.dll`,
  extracted from the app resources), on Linux the `libmpv.so` runtime must be
  installed. `AGUSTREAM_LIBMPV` can point at a specific library.
- Android uses a no-op player until a native video surface is added. The rest
  of the target is wired: `INTERNET` permission, a `Context`-based app directory
  and a persisted session. `AppServices.playbackSupported` is `false` for the
  no-op player, so the UI reports that playback is unavailable instead of
  opening a player that only errors.
- The Nuvio addon metadata/edit screens are still placeholders, as they were
  in the Flutter app.
- **Calendar** uses a Cinemeta convention when an addon exposes it: a
  `calendar-videos` catalog (Cinemeta and AIOMetadata declare it) returns
  `metasDetailed` — the full meta per series — for the library's series in one
  request, which is why `CatalogRepository.detailedItems` exists. When no addon
  exposes it, the section falls back to resolving each series through the
  metadata addons behind a semaphore, so the first episodes appear without
  waiting for the slowest one. Either way only episodes inside a rolling 180-day
  window are kept, episodes without a release date are skipped, and the grid
  fills the window instead of scrolling.

## Improvement backlog

Items that need a design decision, touch several modules, or both, so they are
not a one-file change. They are grouped by the axis they change, not by effort.

### Backends

- **Stremio is a full backend, not just a login.** Swapping Nuvio for Stremio
  already fits the `BackendKind` + `createBackend` seam and needs no `ui/`
  change, but a usable account also needs `LibraryRepository`,
  `ProgressRepository` and `AddonRepository` against the Stremio datastore.
  The auth adapter is the small part; the data repositories are the bulk.

### Product / UX

- **Mobile polish and release hardening.** The compact layout works, but it is
  not tuned surface by surface yet: spacing and proportions still need a pass on
  real phones. Separately, the Android release build is unoptimised —
  `isMinifyEnabled` is `false` and there is no baseline profile — so it reads as
  much slower than it needs to.
- **Internationalisation.** Every user-facing string is a Kotlin literal inside
  a composable; there are no Compose Resources string resources. The migration
  is mechanical but touches every screen in `ui/`, so it should happen before
  the string count grows.
- **Keyboard / D-pad navigation.** `Interaction.kt` covers hover, focus and
  pressed states, and the player has shortcuts, but there is no focus traversal
  across the Home rails and no spatial navigation model. A TV target makes this
  mandatory rather than nice to have.
- **Android predictive back.** There is no back handling at all: the custom
  route stack (`ContentNavigator`) has no `BackHandler`, so the system back
  gesture closes the activity instead of popping a pushed route. It needs a
  `@Composable expect fun` back handler (no-op on desktop) whose Android actual
  delegates to `androidx.activity.compose.BackHandler`, plus the
  `activity-compose` dependency in `app/shared`. Do it before the Android target
  is treated as shippable.

### Player performance

- **OpenGL render backend.** As noted in the player section, the current
  software renderer hands frames to Compose as bitmaps and is CPU-bound. Moving
  to libmpv's OpenGL render backend is the planned performance step and needs a
  GL context wired into the composition root.
