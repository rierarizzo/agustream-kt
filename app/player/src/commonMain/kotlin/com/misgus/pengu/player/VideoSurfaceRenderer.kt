package com.misgus.pengu.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.core.player.VideoOutput

/**
 * A platform video output that draws itself as ordinary Compose content.
 *
 * The engine renders the video into the Compose scene, so it is a normal draw
 * call and any Compose content stacks on top of it: the player controls overlay
 * the video inside the same window, with no native window in between (which
 * would cover them) and no separate overlay window to keep in sync. The desktop
 * mpv backend draws into the Skia canvas and the Android ExoPlayer backend
 * embeds a `PlayerView`, both behind this handle.
 */
interface VideoSurfaceRenderer : VideoOutput {
    /** Draws the current video frame into the Compose scene at [modifier]. */
    @Composable
    fun Draw(modifier: Modifier)
}
