package com.misgus.agustream

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.data.BackendKind
import com.misgus.agustream.data.addons.StremioCatalogRepository
import com.misgus.agustream.data.addons.StremioMetadataRepository
import com.misgus.agustream.data.addons.StremioStreamRepository
import com.misgus.agustream.data.addons.StremioSubtitleRepository
import com.misgus.agustream.data.createBackend
import com.misgus.agustream.data.platform.createHttpClient
import com.misgus.agustream.data.store.AppPaths
import com.misgus.agustream.data.store.FileSessionStore
import com.misgus.agustream.data.store.FileWatchedSeriesCache
import com.misgus.agustream.data.store.JsonMapStore
import com.misgus.agustream.data.store.LocalRatingRepository
import com.misgus.agustream.imaging.configureImageLoader
import com.misgus.agustream.player.MpvPlayerService
import com.misgus.agustream.ui.AgustreamApp
import com.misgus.agustream.ui.services.AppServices
import com.misgus.agustream.ui.theme.AppColors
import kotlinx.coroutines.delay

fun main(args: Array<String>) {
    // The desktop Coil decoder aliases when it downsamples; install the cubic
    // one before any image is rendered.
    configureImageLoader()

    val environment = System.getenv()
    val backendKind = BackendKind.fromEnvironment(environment)
    val httpClient = createHttpClient()

    // The composition root: the only place that picks concrete implementations.
    val backend =
        createBackend(
            kind = backendKind,
            httpClient = httpClient,
            sessionStore = FileSessionStore(),
            localAddons = localAddonsFromEnvironment(environment),
        )

    // One addon list shared by every Stremio repository.
    val addons = backend.addons
    val metadata = StremioMetadataRepository(addons, httpClient)
    val streams = StremioStreamRepository(addons, httpClient)
    val catalogs = StremioCatalogRepository(addons, httpClient)
    val subtitles = StremioSubtitleRepository(addons, httpClient)
    val ratings = LocalRatingRepository(JsonMapStore(AppPaths.ratingsFile))
    val watchedCache = FileWatchedSeriesCache()
    val player = MpvPlayerService()

    val initialSource = initialSource(args)

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 720.dp)
        val windowController = remember { ComposeWindowController(windowState) }
        val services =
            remember {
                AppServices(
                    account = backend.account,
                    library = backend.library,
                    progress = backend.progress,
                    metadata = metadata,
                    streams = streams,
                    catalogs = catalogs,
                    subtitles = subtitles,
                    ratings = ratings,
                    watchedCache = watchedCache,
                    player = player,
                    window = windowController,
                    addons = addons,
                )
            }

        // Prefer a persisted session; fall back to the development shortcut.
        // Running it here (not with `runBlocking` before the window exists)
        // keeps startup responsive and cancellable.
        LaunchedEffect(Unit) {
            val restored =
                runCatchingCancellable { backend.account.restoreSession() }
                    .getOrDefault(false)
            if (!restored) signInFromEnvironment(backend.account, environment)
        }

        Window(
            onCloseRequest = {
                backend.close()
                player.dispose()
                exitApplication()
            },
            state = windowState,
            title = "Agustream",
        ) {
            // AWT does not follow the OS dark mode for the native title bar, so
            // ask DWM. The native peer may not exist on the first frame; retry.
            LaunchedEffect(window) {
                val caption = AppColors.background.toAwtColor()
                val title = AppColors.textPrimary.toAwtColor()
                repeat(DARK_TITLE_BAR_ATTEMPTS) {
                    if (applyDarkTitleBar(window, caption = caption, text = title)) return@LaunchedEffect
                    delay(TITLE_BAR_RETRY_DELAY_MS)
                }
            }
            // Paint the native window with the app background so resizing does
            // not flash the default white surface before Compose's next frame.
            SideEffect {
                applyBackground(window, AppColors.background.toAwtColor())
            }
            AgustreamApp(services = services, initialSource = initialSource)
        }
    }
}

/** Sets [color] as the background of [component] and its Swing children. */
private fun applyBackground(
    component: java.awt.Component,
    color: java.awt.Color,
) {
    component.background = color
    if (component is java.awt.Container) {
        component.components.forEach { applyBackground(it, color) }
    }
}

/** Converts a Compose colour to the AWT colour the window helpers use. */
private fun Color.toAwtColor(): java.awt.Color = java.awt.Color(toArgb(), true)

/**
 * Development shortcut: signs in with `NUVIO_EMAIL` / `NUVIO_PASSWORD`.
 *
 * Credentials are read from the environment for this run only; they are never
 * written to the repository or to disk.
 */
private suspend fun signInFromEnvironment(
    account: com.misgus.agustream.core.repository.AccountRepository,
    environment: Map<String, String>,
) {
    val email = environment["NUVIO_EMAIL"]
    val password = environment["NUVIO_PASSWORD"]
    if (email.isNullOrEmpty() || password.isNullOrEmpty()) return
    runCatchingCancellable { account.signIn(email, password) }
}

/** Addons for local mode from `AGUSTREAM_LOCAL_ADDONS`, comma-separated URLs. */
private fun localAddonsFromEnvironment(environment: Map<String, String>): List<Addon> {
    val raw = environment["AGUSTREAM_LOCAL_ADDONS"] ?: return emptyList()
    return raw.split(',').mapNotNull { entry ->
        val url = entry.trim()
        if (url.isEmpty()) {
            null
        } else {
            Addon(id = url, url = url, name = url.substringAfter("://", url).substringBefore('/'))
        }
    }
}

/** Reads `--play=<source>` from the command line, if present. */
private fun initialSource(args: Array<String>): String? {
    val flag = "--play="
    return args.firstOrNull { it.startsWith(flag) }?.substring(flag.length)
}

/** Frames to wait for the native window peer before giving up on the dark title bar. */
private const val DARK_TITLE_BAR_ATTEMPTS = 20

/** Delay between dark-title-bar retries, in milliseconds. */
private const val TITLE_BAR_RETRY_DELAY_MS = 50L
