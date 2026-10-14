package com.misgus.agustream.ui

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.app_name
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.SignInRequest
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.ui.onboarding.OnboardingScreen
import com.misgus.agustream.ui.platform.systemAnimationsEnabled
import com.misgus.agustream.ui.player.PlayerScreen
import com.misgus.agustream.ui.services.AppServices
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.AppShell
import com.misgus.agustream.ui.shell.RouteViewModelStoreOwner
import com.misgus.agustream.ui.theme.AgustreamTheme
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalMotionEnabled
import com.misgus.agustream.ui.theme.LocalWindowHeight
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.LocalWindowWidth
import com.misgus.agustream.ui.theme.WindowSize
import org.jetbrains.compose.resources.stringResource

/**
 * Root composable.
 *
 * Owns the theme, the app-wide services and the ViewModel store owner. It also
 * resolves the persisted session once, before anything else: until that finishes
 * it shows a splash, then either the onboarding (no session) or the shell. That
 * is the only place a session is bootstrapped, so every target behaves the same.
 */
@Composable
fun AgustreamApp(
    services: AppServices,
    initialSource: String? = null,
    autoSignIn: SignInRequest? = null,
) {
    // Reuse the host's owner when there is one (an Android Activity, which
    // survives configuration changes). Hosts without one, such as desktop, get a
    // composition-scoped fallback.
    val fallbackOwner = remember { RouteViewModelStoreOwner() }
    DisposableEffect(fallbackOwner) {
        onDispose { fallbackOwner.viewModelStore.clear() }
    }
    val owner = LocalViewModelStoreOwner.current ?: fallbackOwner
    val settings by services.settings.state.collectAsStateWithLifecycle()
    val account by services.account.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { services.settings.load() }

    // Resolve the session before deciding what to show, so a returning user never
    // sees the onboarding for a frame. `autoSignIn` is the desktop development
    // shortcut, applied only when there is no stored session.
    var bootstrapping by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val restored = runCatchingCancellable { services.account.restoreSession() }.getOrDefault(false)
        if (!restored && autoSignIn != null) {
            runCatchingCancellable { services.account.signIn(autoSignIn) }
        }
        bootstrapping = false
    }

    CompositionLocalProvider(
        LocalAppServices provides services,
        LocalViewModelStoreOwner provides owner,
        LocalMotionEnabled provides (settings.animationsEnabled && systemAnimationsEnabled()),
    ) {
        AgustreamTheme(theme = settings.theme) {
            // Publish the window class and insets once, at the root, so every
            // surface — the shell, onboarding and the player — reads the same one.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val windowSize = WindowSize.of(maxWidth)
                CompositionLocalProvider(
                    LocalWindowSize provides windowSize,
                    LocalWindowWidth provides maxWidth,
                    LocalWindowHeight provides maxHeight,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        when {
                            initialSource != null ->
                                PlayerScreen(target = PlaybackTarget.raw(initialSource), onBack = {})
                            bootstrapping -> BootSplash()
                            !account.isSignedIn -> OnboardingScreen()
                            else -> AppShell()
                        }
                    }
                }
            }
        }
    }
}

/** Minimal splash shown while the persisted session is resolved. */
@Composable
private fun BootSplash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.app_name), style = AppType.screenTitle())
            Spacer(Modifier.height(AppSpacing.lg))
            CircularProgressIndicator()
        }
    }
}
