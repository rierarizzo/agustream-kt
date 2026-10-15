package com.misgus.pengu.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.core.player.PlayerService

/**
 * Draws the player's video output as Compose content.
 *
 * The engine renders the video into the Compose canvas (mediamp's mpv backend),
 * so it is a normal draw call inside this window and the Compose controls are
 * layered on top of it.
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
