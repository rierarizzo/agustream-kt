package com.misgus.agustream.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.ui.profiles.ProfilePicker
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.streams.StreamsDialog
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Persistent app chrome: navigation rail, title bar and the content area.
 *
 * The content area hosts the section and any pushed route; the rail and title
 * bar stay visible, and dialogs cover the whole window because they are hosted
 * at the root.
 */
@Composable
fun AppShell() {
    val services = LocalAppServices.current
    val account by services.account.state.collectAsStateWithLifecycle()
    val isFullScreen by services.window.isFullScreen.collectAsStateWithLifecycle()
    val immersive = services.chrome.immersive

    var selected by remember { mutableStateOf(AppSection.HOME) }
    val navigator = remember { ContentNavigator() }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var streamsRequest by remember { mutableStateOf<StreamsRequest?>(null) }

    val hideRail = isFullScreen || immersive

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHost) {
        if (account.isSignedIn && account.requiresProfile && account.activeProfile == null) {
            // Profile gate: the library and every write belong to a profile, so
            // there is no safe default. Backends without profiles never enter here.
            Column(Modifier.fillMaxSize().background(AppColors.background)) {
                Box(Modifier.weight(1f)) { ProfilePicker() }
            }
        } else {
            Row(Modifier.fillMaxSize().background(AppColors.background)) {
                if (!hideRail) {
                    SideRail(
                        selected = selected,
                        onSelected = {
                            selected = it
                            navigator.clear()
                        },
                        activeProfile = account.activeProfile,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.background,
                        snackbarHost = { SnackbarHost(snackbarHost) },
                    ) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            ContentArea(
                                selected = selected,
                                navigator = navigator,
                                onOpenStreams = { streamsRequest = it },
                            )
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
