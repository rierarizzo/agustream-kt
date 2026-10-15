package com.misgus.pengu.ui.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.error_playback_unavailable
import com.misgus.pengu.core.player.PlayerService
import org.jetbrains.compose.resources.stringResource

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
        Text(stringResource(Res.string.error_playback_unavailable))
    }
}
