package com.misgus.agustream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.misgus.agustream.core.player.NoopPlayerService
import com.misgus.agustream.data.BackendKind
import com.misgus.agustream.data.addons.StremioCatalogRepository
import com.misgus.agustream.data.addons.StremioMetadataRepository
import com.misgus.agustream.data.addons.StremioStreamRepository
import com.misgus.agustream.data.addons.StremioSubtitleRepository
import com.misgus.agustream.data.createBackend
import com.misgus.agustream.data.platform.createHttpClient
import com.misgus.agustream.data.store.NoopSessionStore
import com.misgus.agustream.ui.AgustreamApp
import com.misgus.agustream.ui.services.AppServices
import com.misgus.agustream.ui.window.NoopWindowController

/**
 * Android entry point.
 *
 * Desktop keeps its own composition root in `:app:desktopApp`; this one wires
 * the shared backend and a no-op player so the app compiles and runs until a
 * native video surface is added.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val httpClient = createHttpClient()
        val backend = createBackend(
            kind = BackendKind.NUVIO,
            httpClient = httpClient,
            sessionStore = NoopSessionStore(),
        )
        val addons = backend.addons
        val services = AppServices(
            account = backend.account,
            library = backend.library,
            progress = backend.progress,
            metadata = StremioMetadataRepository(addons, httpClient),
            streams = StremioStreamRepository(addons, httpClient),
            catalogs = StremioCatalogRepository(addons, httpClient),
            subtitles = StremioSubtitleRepository(addons, httpClient),
            player = NoopPlayerService(),
            window = NoopWindowController(),
            addons = addons,
        )

        setContent {
            AgustreamApp(services = services)
        }
    }
}
