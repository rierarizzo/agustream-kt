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
  `=`/`&` raw.
- **Nuvio/Supabase**: raw HTTP (`NuvioClient`) — discovery, Supabase Auth,
  refresh, PostgREST select/insert/delete and the `sync_*` RPCs — and
  capability repositories scoped to the active profile. Session tokens live in
  `data/`, never in the domain.
- **Local store**: `JsonFileStore`/`JsonMapStore` and the session / ratings /
  watched-series stores. Path resolution is a platform `expect`
  (`%APPDATA%\Agustream` on Windows, XDG elsewhere). Writes go through a
  sibling temporary file and are renamed into place, so an interrupted write
  cannot leave a truncated document, and a failed write is reported instead of
  being swallowed. Read-modify-write cycles in the local repositories are
  serialized with a `Mutex`. The session file is restricted to the current user
  where the OS allows it; that is best-effort hardening, not a keychain.
- **Session renewal**: `NuvioClient` renews the token before a request when it
  has expired, and once more when the backend answers `401`. Renewals are
  serialized with a mutex so parallel requests do not refresh twice. Engines are
  configured with finite connect/request/socket timeouts.
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
  Library, Discover, Calendar) wait for a profile before loading rather than
  loading without one and failing.

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
- **Navigation** is a small typed stack (`ContentNavigator` + `ContentRoute`)
  rendered inside the shell content area. Sections keep the top navigation
  visible; a pushed route (detail, catalog) hides it, so an immersive hero such
  as the detail one fills the whole window — the same behaviour as the Flutter
  nested `Navigator`, without adding a navigation library.
- **Discover** flattens every catalog the enabled addons declare into a single
  mixed grid, like the Library, so the addon catalog boundaries stay invisible.
  Only browsable catalogs are used: one whose extras are required (a search
  term, a list of ids, ...) cannot be fetched without a query, so Home and
  Discover skip it instead of asking and getting an empty page. The catalogs are
  interleaved across addons before the cap, so one addon with many catalogs does
  not crowd out the rest, and the items are interleaved round-robin and deduped,
  so the grid alternates sources instead of showing one catalog as a block. It
  pages with `skip` as the grid is scrolled, so it keeps growing instead of
  stopping at the first page. **Search** aggregates the catalogs that declare
  the `search` extra in parallel, debounced and deduped across addons, and
  splits the results into a Movies rail and a Series rail. **Calendar** paints a
  month grid with the upcoming episodes of the Library's series; see the
  limitation below.
- **Theme** centralises the design tokens (`AppColors`, `AppSpacing`,
  `AppRadii`, `AppSizes`, `PosterCaption`) and the Inter typeface loaded
  from Compose Resources. Material3 `visualDensity` stays at the platform default.
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
  the bitmap memory the renderer has to scale every frame.
- **Window**: the app keeps the **native window decoration** (title bar,
  minimize, maximize, close and resizing are OS-managed), which avoids the
  glitches a custom title bar can cause on Windows. Compose Desktop draws its
  own resizers only when the window is undecorated, so nothing custom is needed.
  The only window behaviour routed through `WindowController` is **fullscreen**,
  which the player owns; the previous placement is restored on exit. On Windows
  the native title bar is opted into the DWM immersive dark mode and, on
  Windows 11, tinted to the app background (`WindowAppearance.kt`), because AWT
  does not follow the OS dark mode on its own.

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
- **Calendar has no protocol source.** Stremio exposes no calendar resource, so
  the section is derived from the Library: each saved series is resolved through
  the metadata addons and its episodes inside a rolling 180-day window are
  painted on a month grid. Episodes without a release date are skipped. The
  lookups run behind a semaphore and the grid paints as each series resolves, so
  the first episodes appear without waiting for the slowest addon, and the grid
  fills the window instead of scrolling.

## Improvement backlog

Items that need a design decision, touch several modules, or both, so they are
not a one-file change. They are grouped by the axis they change, not by effort.

### Domain contracts

- **The sign-in method is baked into the account contract.**
  `AccountRepository.signIn(email, password)` assumes email + password. An
  OAuth, magic-link or device-pairing flow does not fit the signature, so
  supporting one means replacing it with a request object or a flow-based
  operation and updating every implementation plus the sign-in card. Doing it
  while only one backend exists is cheap; doing it after a second one lands is
  not.

### Backends

- **Stremio is a full backend, not just a login.** Swapping Nuvio for Stremio
  already fits the `BackendKind` + `createBackend` seam and needs no `ui/`
  change, but a usable account also needs `LibraryRepository`,
  `ProgressRepository` and `AddonRepository` against the Stremio datastore.
  The auth adapter is the small part; the data repositories are the bulk.
- **`NuvioClient` is a single responsibility too many.** Discovery, Supabase
  auth, token refresh, PostgREST select/insert/delete and the `sync_*` RPCs live
  in one 383-line class. Every repository depending on it is reachable from the
  same seam, so splitting it later is mechanical, but it is not a one-commit
  change.

### Product / UX

- **First-run onboarding.** The login is still the temporary card inside
  Settings, and Home only tells the user to "sign in from Settings". A new user
  has to guess where to go. The architecture is ready (a not-signed-in state and
  a profile gate already exist); what is missing is a dedicated welcome/sign-in
  route selected before the shell, or at minimum a CTA in the empty state.
- **Internationalisation.** Every user-facing string is a Kotlin literal inside
  a composable; there are no Compose Resources string resources. The migration
  is mechanical but touches every screen in `ui/`, so it should happen before
  the string count grows.
- **Adaptive layouts.** Rails, poster grids and the hero use fixed sizes; only
  the top navigation scrolls horizontally. A narrow window (or a future tablet
  / TV target) needs width-aware column counts and rail heights.
- **Keyboard / D-pad navigation.** `Interaction.kt` covers hover, focus and
  pressed states, and the player has shortcuts, but there is no focus traversal
  across the Home rails and no spatial navigation model. A TV target makes this
  mandatory rather than nice to have.

### Player performance

- **OpenGL render backend.** As noted in the player section, the current
  software renderer hands frames to Compose as bitmaps and is CPU-bound. Moving
  to libmpv's OpenGL render backend is the planned performance step and needs a
  GL context wired into the composition root.

### Motion

- **Animations are deliberately absent for now.** The UI switches state
  instantly. Planned, roughly in priority order: route transitions
  (detail/catalog/player), a hover scale on posters, loading skeletons with a
  shimmer, fade/scale on dialogs and a fade-in of the detail content. All of it
  must use the cheap path — `graphicsLayer` transforms, no layout animation and
  no per-frame recomposition — and infinite animations must be scoped to what is
  on screen. The detail backdrop blur is already the most expensive effect in
  the UI, so motion should not add to that cost.
