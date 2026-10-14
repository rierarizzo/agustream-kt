package com.misgus.agustream

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.repository.SignInRequest
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
import com.misgus.agustream.data.store.JsonSettingsRepository
import com.misgus.agustream.data.store.LocalRatingRepository
import com.misgus.agustream.imaging.configureImageLoader
import com.misgus.agustream.player.MpvPlayerService
import com.misgus.agustream.ui.AgustreamApp
import com.misgus.agustream.ui.services.AppServices
import com.misgus.agustream.ui.theme.AppWindowChrome
import kotlinx.coroutines.delay
import java.awt.Dimension

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
    val settings = JsonSettingsRepository(JsonMapStore(AppPaths.settingsFile, pretty = true))
    val watchedCache = FileWatchedSeriesCache()
    val player = MpvPlayerService()

    val initialSource = initialSource(args)

    application {
        val windowState = rememberWindowState(width = WINDOW_WIDTH, height = WINDOW_HEIGHT)
        val scope = rememberCoroutineScope()
        val windowController = remember { ComposeWindowController(windowState, scope) }
        val services =
            remember {
                AppServices(
                    account = backend.account,
                    settings = settings,
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

        Window(
            onCloseRequest = {
                backend.close()
                player.dispose()
                exitApplication()
            },
            state = windowState,
            title = WINDOW_TITLE,
        ) {
            // The OS enforces the minimum: the window can grow or maximise but
            // never shrink below the size it opens with. Reapplied when the
            // density changes (e.g. moving to a monitor with another scale).
            val density = LocalDensity.current
            LaunchedEffect(window, density) {
                window.minimumSize =
                    Dimension(
                        with(density) { WINDOW_WIDTH.roundToPx() },
                        with(density) { WINDOW_HEIGHT.roundToPx() },
                    )
            }
            // AWT does not follow the OS dark mode for the native title bar, so
            // ask DWM. The native peer may not exist on the first frame; retry.
            LaunchedEffect(window) {
                val caption = AppWindowChrome.titleBar.toAwtColor()
                val title = AppWindowChrome.foreground.toAwtColor()
                val border = AppWindowChrome.border.toAwtColor()
                repeat(DARK_TITLE_BAR_ATTEMPTS) {
                    if (applyDarkTitleBar(window, caption = caption, text = title, border = border)) return@LaunchedEffect
                    delay(TITLE_BAR_RETRY_DELAY_MS)
                }
            }
            // Paint the native window with the app background so resizing does
            // not flash the default white surface before Compose's next frame.
            SideEffect {
                applyBackground(window, AppWindowChrome.windowBackground.toAwtColor())
            }
            AgustreamApp(
                services = services,
                initialSource = initialSource,
                autoSignIn = credentialsFromEnvironment(environment),
            )
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
 * Development shortcut: signs in with `NUVIO_EMAIL` / `NUVIO_PASSWORD` when there
 * is no stored session.
 *
 * Credentials are read from the environment for this run only; they are never
 * written to the repository or to disk.
 */
private fun credentialsFromEnvironment(environment: Map<String, String>): SignInRequest? {
    val email = environment["NUVIO_EMAIL"]
    val password = environment["NUVIO_PASSWORD"]
    if (email.isNullOrEmpty() || password.isNullOrEmpty()) return null
    return SignInRequest.EmailPassword(email, password)
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

/** Size the desktop window opens with, and the smallest it can be resized to. */
private val WINDOW_WIDTH: Dp = 1280.dp
private val WINDOW_HEIGHT: Dp = 720.dp

/**
 * Window title. Kept local because the shared Compose resources are internal to
 * the `:app:shared` module.
 */
private const val WINDOW_TITLE = "Agustream"

/** Delay between dark-title-bar retries, in milliseconds. */
private const val TITLE_BAR_RETRY_DELAY_MS = 50L
