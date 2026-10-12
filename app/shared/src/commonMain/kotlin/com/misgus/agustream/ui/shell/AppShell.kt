package com.misgus.agustream.ui.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.ui.profiles.ProfilePicker
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsDialog
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.LocalMotionEnabled
import kotlinx.coroutines.launch

/**
 * Persistent app chrome: the top navigation and the content area.
 *
 * The content area fills the whole window and the top navigation floats over it,
 * so showing or hiding the bar never changes the content height. That matters:
 * resizing the content viewport clamps a section's scroll (it was scrolled back
 * up when a route hid the bar). Sections reserve the bar's height with padding;
 * pushed routes use the full window and let the bar fade away.
 */
@Composable
fun AppShell() {
    val services = LocalAppServices.current
    val account by services.account.state.collectAsStateWithLifecycle()
    val isFullScreen by services.window.isFullScreen.collectAsStateWithLifecycle()
    val immersive = services.chrome.immersive
    val motionEnabled = LocalMotionEnabled.current

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

    Box(Modifier.fillMaxSize().background(AppColors.background)) {
        CompositionLocalProvider(LocalSnackbarHost provides snackbarHost) {
            if (gate) {
                // Profile gate: the library and every write belong to a profile, so
                // there is no safe default. Backends without profiles never enter here.
                Box(Modifier.fillMaxSize()) { ProfilePicker() }
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbarHost) },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        ContentArea(
                            selected = selected,
                            navigator = navigator,
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
                        // Floating top bar: it never changes the content height,
                        // it just fades in and out over it.
                        val navFade = AppMotion.spec<Float>(motionEnabled, AppMotion.MEDIUM)
                        val navSlide = AppMotion.spec<IntOffset>(motionEnabled, AppMotion.MEDIUM)
                        AnimatedVisibility(
                            visible = showNav,
                            modifier = Modifier.align(Alignment.TopCenter),
                            enter = fadeIn(navFade) + slideInVertically(navSlide) { -it },
                            exit = fadeOut(navFade) + slideOutVertically(navSlide) { -it },
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                TopNav(
                                    selected = selected,
                                    onSelected = selectSection,
                                    activeProfile = account.activeProfile,
                                )
                                // Shadow cast by the bar over the content below it.
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(AppSizes.topBarShadow)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Black.copy(alpha = AppAlpha.TOP_BAR_SHADOW), Color.Transparent),
                                            ),
                                        ),
                                )
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
