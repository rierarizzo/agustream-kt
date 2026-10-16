package com.misgus.pengu.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.core.player.PlayerService
import com.misgus.pengu.player.VideoSurfaceRenderer

/**
 * Draws the player's video output as Compose content.
 *
 * On Android the ExoPlayer backend embeds a `PlayerView` through `AndroidView`,
 * which the engine's [VideoSurfaceRenderer] exposes, so the Compose controls
 * stack on top of it.
 */
@Composable
actual fun VideoSurface(
    player: PlayerService,
    modifier: Modifier,
) {
    val renderer = player.videoOutput as? VideoSurfaceRenderer
    if (renderer == null) {
        EmptyVideoSurface(modifier)
        return
    }
    renderer.Draw(modifier)
}
