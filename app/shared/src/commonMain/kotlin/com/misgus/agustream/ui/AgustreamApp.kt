package com.misgus.agustream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.ui.player.PlayerScreen
import com.misgus.agustream.ui.services.AppServices
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.AppShell
import com.misgus.agustream.ui.shell.RouteViewModelStoreOwner
import com.misgus.agustream.ui.theme.AgustreamTheme
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.LocalMotionEnabled

/**
 * Root composable.
 *
 * Owns the theme, the app-wide services and the ViewModel store owner, and is
 * pure wiring: no business logic belongs here.
 */
@Composable
fun AgustreamApp(
    services: AppServices,
    initialSource: String? = null,
) {
    val owner = remember { RouteViewModelStoreOwner() }
    DisposableEffect(Unit) {
        onDispose { owner.viewModelStore.clear() }
    }
    val settings by services.settings.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { services.settings.load() }

    CompositionLocalProvider(
        LocalAppServices provides services,
        LocalViewModelStoreOwner provides owner,
        LocalMotionEnabled provides settings.animationsEnabled,
    ) {
        AgustreamTheme {
            Surface(
                modifier = Modifier.fillMaxSize().background(AppColors.background),
                color = AppColors.background,
            ) {
                if (initialSource != null) {
                    PlayerScreen(target = PlaybackTarget.raw(initialSource), onBack = {})
                } else {
                    AppShell()
                }
            }
        }
    }
}
