package com.misgus.pengu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.misgus.pengu.core.player.NoopPlayerService
import com.misgus.pengu.data.BackendBundle
import com.misgus.pengu.data.BackendKind
import com.misgus.pengu.data.addons.StremioCatalogRepository
import com.misgus.pengu.data.addons.StremioMetadataRepository
import com.misgus.pengu.data.addons.StremioStreamRepository
import com.misgus.pengu.data.addons.StremioSubtitleRepository
import com.misgus.pengu.data.createBackend
import com.misgus.pengu.data.platform.createHttpClient
import com.misgus.pengu.data.platform.initPlatform
import com.misgus.pengu.data.store.FileSessionStore
import com.misgus.pengu.data.store.AppPaths
import com.misgus.pengu.data.store.JsonMapStore
import com.misgus.pengu.data.store.JsonSettingsRepository
import com.misgus.pengu.ui.PenguApp
import com.misgus.pengu.ui.services.AppServices
import com.misgus.pengu.ui.window.NoopWindowController

/**
 * Android entry point.
 *
 * Desktop keeps its own composition root in `:app:desktopApp`; this one wires
 * the shared backend and a no-op player. The player reports a clear error until
 * a native video surface is added; [AppServices.playbackSupported] lets the UI
 * tell the user instead of failing silently.
 */
class MainActivity : ComponentActivity() {
    private lateinit var backend: BackendBundle

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app always paints a dark surface, so the system-bar icons are
        // forced light: the default `auto` follows the OS and turns them dark in
        // light mode, which is invisible over the artwork.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Gives the platform layer a real, persistent app directory.
        initPlatform(this)

        val httpClient = createHttpClient()
        backend = createBackend(
            kind = BackendKind.NUVIO,
            httpClient = httpClient,
            sessionStore = FileSessionStore(),
        )
        val addons = backend.addons
        val settings = JsonSettingsRepository(JsonMapStore(AppPaths.settingsFile, pretty = true))
        val services = AppServices(
            account = backend.account,
            settings = settings,
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
            PenguApp(services = services)
        }
    }

    override fun onDestroy() {
        // Releases the HTTP client owned by the backend.
        backend.close()
        super.onDestroy()
    }
}
