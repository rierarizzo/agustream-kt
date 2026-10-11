package com.misgus.agustream.ui.video

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.misgus.agustream.core.player.PlayerService

/**
 * Draws the player's video output as Compose content.
 *
 * The engine renders frames into an [ImageBitmap] (libmpv's render API) instead
 * of a native heavyweight surface, so Compose controls stay on top.
 */
@Composable
actual fun VideoSurface(
    player: PlayerService,
    modifier: Modifier,
) {
    val source = player.videoOutput as? FrameSource
    if (source == null) {
        EmptyVideoSurface(modifier)
        return
    }
    val frame by source.frames.collectAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    Canvas(modifier.onSizeChanged { size = it }) {
        // Reading `frame` ties the draw to the frame counter, so a new frame
        // repaints even if the size did not change.
        if (frame < 0L || size.width <= 0 || size.height <= 0) return@Canvas
        val image = source.render(size.width, size.height)
        if (image != null) {
            drawImage(image, dstSize = size)
        }
    }
}
