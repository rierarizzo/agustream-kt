package com.misgus.agustream

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.misgus.agustream.core.model.Addon
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
import com.misgus.agustream.player.VlcjPlayerService
import com.misgus.agustream.ui.AgustreamApp
import com.misgus.agustream.ui.services.AppServices
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val environment = System.getenv()
    val backendKind = BackendKind.fromEnvironment(environment)
    val httpClient = createHttpClient()

    // The composition root: the only place that picks concrete implementations.
    val backend = createBackend(
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
    val player = VlcjPlayerService()

    // Prefer a persisted session; fall back to the development shortcut.
    val restored = runBlocking { backend.account.restoreSession() }
    if (!restored) signInFromEnvironment(backend.account, environment)

    val initialSource = initialSource(args)

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 720.dp)
        val windowController = remember { ComposeWindowController(windowState) }
        val services = remember {
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

        Window(
            onCloseRequest = {
                backend.close()
                player.dispose()
                exitApplication()
            },
            state = windowState,
            title = "Agustream",
        ) {
            // Paint the native window with the app background so resizing does
            // not flash the default white surface before Compose's next frame.
            SideEffect {
                val background = java.awt.Color(0x0A, 0x0A, 0x0C)
                applyBackground(window, background)
            }
            AgustreamApp(services = services, initialSource = initialSource)
        }
    }
}

/** Sets [color] as the background of [component] and its Swing children. */
private fun applyBackground(component: java.awt.Component, color: java.awt.Color) {
    component.background = color
    if (component is java.awt.Container) {
        component.components.forEach { applyBackground(it, color) }
    }
}

/**
 * Development shortcut: signs in with `NUVIO_EMAIL` / `NUVIO_PASSWORD`.
 *
 * Credentials are read from the environment for this run only; they are never
 * written to the repository or to disk.
 */
private fun signInFromEnvironment(
    account: com.misgus.agustream.core.repository.AccountRepository,
    environment: Map<String, String>,
) {
    val email = environment["NUVIO_EMAIL"]
    val password = environment["NUVIO_PASSWORD"]
    if (email.isNullOrEmpty() || password.isNullOrEmpty()) return
    runBlocking {
        runCatching { account.signIn(email, password) }
    }
}

/** Addons for local mode from `AGUSTREAM_LOCAL_ADDONS`, comma-separated URLs. */
private fun localAddonsFromEnvironment(environment: Map<String, String>): List<Addon> {
    val raw = environment["AGUSTREAM_LOCAL_ADDONS"] ?: return emptyList()
    return raw.split(',').mapNotNull { entry ->
        val url = entry.trim()
        if (url.isEmpty()) null
        else Addon(id = url, url = url, name = url.substringAfter("://", url).substringBefore('/'))
    }
}

/** Reads `--play=<source>` from the command line, if present. */
private fun initialSource(args: Array<String>): String? {
    val flag = "--play="
    return args.firstOrNull { it.startsWith(flag) }?.substring(flag.length)
}
