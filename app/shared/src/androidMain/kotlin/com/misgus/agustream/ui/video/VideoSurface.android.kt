package com.misgus.agustream.ui.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.misgus.agustream.core.player.PlayerService

/**
 * Android has no native video surface wired up yet; the abstraction is in place
 * so a `PlayerView`-backed actual can be added later without touching the UI.
 */
@Composable
actual fun VideoSurface(
    player: PlayerService,
    modifier: Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Video playback is not available on this target yet.")
    }
}
