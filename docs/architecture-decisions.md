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
:app:desktopApp  composition root: VLCJ player, window control, main()
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
  (`%APPDATA%\Agustream` on Windows, XDG elsewhere).
- **`BackendKind` + `createBackend`** is the single place that knows the
  concrete implementations, replacing the Flutter `backend_factory.dart`.

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
  rendered inside the shell content area, so the rail and title bar stay
  visible — the same behaviour as the Flutter nested `Navigator`, without adding
  a navigation library.
- **Theme** centralises the design tokens (`AppColors`, `AppSpacing`,
  `AppRadii`, `AppSizes`, `PosterCaption`) and the IBM Plex Sans typeface loaded
  from Compose Resources. Material3 `visualDensity` stays at the platform default.
- **Images with Coil 3** (`AsyncImage`), the multiplatform image loader.
- **Window**: the app keeps the **native window decoration** (title bar,
  minimize, maximize, close and resizing are OS-managed), which avoids the
  glitches a custom title bar can cause on Windows. Compose Desktop draws its
  own resizers only when the window is undecorated, so nothing custom is needed.
  The only window behaviour routed through `WindowController` is **fullscreen**,
  which the player owns; the previous placement is restored on exit.

## Player

`PlayerService` is implemented on desktop by `VlcjPlayerService`
(libvlc via VLCJ), in the composition root so the engine stays out of the shared
UI. It is created lazily and only when VLC is discoverable, so a machine without
VLC degrades to a clear playback error. The video surface is an `expect`
composable: on desktop it embeds the engine's AWT `Component` through
`SwingPanel`, which keeps `:app:shared` independent from VLCJ. Watch progress
reporting, resume, subtitles/audio selection and next-episode autoplay are
ported from the original.

## Testing

`commonTest` covers the pure domain (playback target, watched rules, year
parsing, badge resolver with fakes) and the defensive parsers/mappers.
`jvmTest` covers the file stores and the local repositories. `:app:shared`
`commonTest` covers the section ViewModels with in-memory repositories and
`Dispatchers.setMain`.

## Known differences / limitations

- VLCJ requires VLC to be installed; without it the player reports an error.
- Android uses a no-op player and a temporary filesystem directory until a
  native video surface and proper `Context`-based paths are added.
- The Nuvio addon metadata/edit screens and the Discover/Search/Calendar
  sections are still placeholders, as they were in the Flutter app.
