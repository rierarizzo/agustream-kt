package com.misgus.agustream.ui.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.NavStyle
import com.misgus.agustream.ui.profiles.ProfilePicker
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsDialog
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.LocalMotionEnabled
import com.misgus.agustream.ui.theme.LocalWindowHeight
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.LocalWindowWidth
import com.misgus.agustream.ui.theme.WindowSize
import kotlinx.coroutines.launch

/**
 * Persistent app chrome: the navigation and the content area.
 *
 * The content area fills the whole window and the navigation floats over it, so
 * showing or hiding it never changes the content viewport. That matters:
 * resizing the viewport clamps a section's scroll (it was scrolled back up when
 * a route hid the bar). Sections reserve the bar's height (or the rail's width)
 * with padding; pushed routes use the full window and let the bar fade away.
 */
@Composable
fun AppShell() {
    val services = LocalAppServices.current
    val account by services.account.state.collectAsStateWithLifecycle()
    val settings by services.settings.state.collectAsStateWithLifecycle()
    val isFullScreen by services.window.isFullScreen.collectAsStateWithLifecycle()
    val immersive = services.chrome.immersive
    val motionEnabled = LocalMotionEnabled.current
    val navStyle = settings.navStyle

    var selected by remember { mutableStateOf(AppSection.HOME) }
    val navigator = remember { ContentNavigator() }
    val selectSection: (AppSection) -> Unit = {
        selected = it
        navigator.clear()
    }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var streamsRequest by remember { mutableStateOf<StreamsRequest?>(null) }

    // A pushed route (detail, catalog) is an immersive screen with its own back
    // affordance, so the top navigation fades away; the content keeps its size.
    val showNav = !isFullScreen && !immersive && navigator.current == null
    val gate = account.isSignedIn && account.requiresProfile && account.activeProfile == null

    BoxWithConstraints(Modifier.fillMaxSize().background(AppColors.background)) {
        val windowSize = WindowSize.of(maxWidth)
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHost,
            LocalWindowSize provides windowSize,
            LocalWindowWidth provides maxWidth,
            LocalWindowHeight provides maxHeight,
        ) {
            if (gate) {
                // Profile gate: the library and every write belong to a profile, so
                // there is no safe default. Backends without profiles never enter here.
                Box(Modifier.fillMaxSize()) { ProfilePicker() }
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    // The app is edge-to-edge: screens place the system insets
                    // themselves, so the Scaffold must not reserve them again.
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = {
                        SnackbarHost(snackbarHost, modifier = Modifier.navigationBarsPadding())
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        ContentArea(
                            selected = selected,
                            navigator = navigator,
                            navStyle = navStyle,
                            onOpenStreams = { request ->
                                if (services.playbackSupported) {
                                    streamsRequest = request
                                } else {
                                    scope.launch {
                                        snackbarHost.showSnackbar(
                                            "Playback is not available on this target yet.",
                                        )
                                    }
                                }
                            },
                        )
                        // The navigation never changes the content viewport: it
                        // fades in over the space the sections reserve. Compact
                        // windows (phones) always get the icon-only bottom bar;
                        // wider windows pick the rail or the top bar in settings.
                        val navFade = AppMotion.spec<Float>(motionEnabled, AppMotion.MEDIUM)
                        val navSlide = AppMotion.spec<IntOffset>(motionEnabled, AppMotion.MEDIUM)
                        if (windowSize == WindowSize.COMPACT) {
                            AnimatedVisibility(
                                visible = showNav,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                enter = fadeIn(navFade) + slideInVertically(navSlide) { it },
                                exit = fadeOut(navFade) + slideOutVertically(navSlide) { it },
                            ) {
                                BottomNav(selected = selected, onSelected = selectSection)
                            }
                        } else {
                            val useRail = navStyle == NavStyle.RAIL
                            val slideIn =
                                if (useRail) {
                                    slideInHorizontally(navSlide) { -it }
                                } else {
                                    slideInVertically(navSlide) { -it }
                                }
                            val slideOut =
                                if (useRail) {
                                    slideOutHorizontally(navSlide) { -it }
                                } else {
                                    slideOutVertically(navSlide) { -it }
                                }
                            AnimatedVisibility(
                                visible = showNav,
                                modifier = Modifier.align(if (useRail) Alignment.CenterStart else Alignment.TopCenter),
                                enter = fadeIn(navFade) + slideIn,
                                exit = fadeOut(navFade) + slideOut,
                            ) {
                                if (useRail) {
                                    LeftNav(
                                        selected = selected,
                                        onSelected = selectSection,
                                        activeProfile = account.activeProfile,
                                    )
                                } else {
                                    TopNav(
                                        selected = selected,
                                        onSelected = selectSection,
                                        activeProfile = account.activeProfile,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    streamsRequest?.let { request ->
        StreamsDialog(
            request = request,
            onDismiss = { streamsRequest = null },
            onSelect = { stream ->
                streamsRequest = null
                val url = stream.url
                if (!url.isNullOrEmpty()) {
                    navigator.push(
                        ContentRoute.Player(
                            PlaybackTarget.fromContent(
                                type = request.type,
                                id = request.id,
                                title = request.title,
                                source = url,
                                httpHeaders = stream.httpHeaders,
                            ),
                        ),
                    )
                } else {
                    scope.launch {
                        snackbarHost.showSnackbar(
                            "This source cannot be played yet (torrent or external link).",
                        )
                    }
                }
            },
        )
    }
}
