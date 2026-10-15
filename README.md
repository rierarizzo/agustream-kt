# Pengu

A desktop client for Windows 11 (and Linux later) that connects to the **Nuvio**
backend, is compatible with the **Stremio addon protocol**, and plays with
**libmpv**. Complementary to Nuvio, not a replacement.

This repository is the **Compose Multiplatform** implementation, migrated from
the original Flutter app. Desktop is the only target wired up end to end for
now; the shared code is multiplatform and the Android app compiles against it.

## Stack

| Layer | Technology |
| --- | --- |
| UI | Compose Multiplatform + Material 3, Inter |
| State | `androidx.lifecycle` ViewModel + `StateFlow` |
| HTTP | Ktor client (CIO on desktop, OkHttp on Android) |
| JSON | kotlinx.serialization (defensive `JsonElement` parsing) |
| Images | Coil 3 |
| Playback | libmpv through JNA (desktop), isolated behind `PlayerService` |
| Accounts/sync | Nuvio API (Supabase) |
| Sources | Stremio addons (HTTP/JSON) |

## Modules

```
app/desktopApp   composition root: window, libmpv player, main()
app/androidApp   Android composition root
app/fonts        IBM Plex Sans assets as Compose Resources (kept for comparison)
app/shared       Compose UI and ViewModels
core             pure domain: models, repository contracts, watched rules
data             Stremio, Nuvio and local implementations
```

The dependency direction is `ui -> domain` and `data -> domain`; only the
composition roots reference `data`. See
[`docs/architecture-decisions.md`](docs/architecture-decisions.md) for the full
decision record.

## Running

- Desktop: `./gradlew :app:desktopApp:run`
- Android: `./gradlew :app:androidApp:assembleDebug`

Development shortcuts (environment variables, read for the run only):

- `PENGU_BACKEND=local` runs everything on this machine with no account.
- `PENGU_LOCAL_ADDONS=<manifest-url>[,<manifest-url>...]` gives local mode
  catalogs.
- `NUVIO_EMAIL` / `NUVIO_PASSWORD` signs in without going through Settings.
- `--play=<url-or-path>` opens straight into the player.

## Tests

- Domain: `./gradlew :core:jvmTest`
- Data: `./gradlew :data:jvmTest`
- UI/ViewModels: `./gradlew :app:shared:jvmTest`
- Desktop player helpers: `./gradlew :app:desktopApp:test`

## Quality gates

- Lint and formatting: `./gradlew ktlintCheck` (apply with `./gradlew ktlintFormat`).
- CI (`.github/workflows/ci.yml`) runs lint, the three test suites, the desktop
  compile and the Android debug build.

## References

- Stremio addon protocol: `https://github.com/Stremio/stremio-addon-sdk/blob/master/docs/protocol.md`
- Nuvio backend (self-host): `https://github.com/NuvioMedia/self-host`
- Compose Multiplatform: `https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html`

## License

Pengu is licensed under the [GNU General Public License v3.0](LICENSE)
(SPDX: `GPL-3.0`).

Bundled third-party components (fonts, icons and the libmpv library used for
playback) keep their own licenses; see [`licenses/`](licenses/README.md).
