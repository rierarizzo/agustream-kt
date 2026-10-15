package com.misgus.pengu.ui.video

import androidx.compose.ui.graphics.ImageBitmap
import com.misgus.pengu.core.player.VideoOutput
import kotlinx.coroutines.flow.StateFlow

/**
 * A source of video frames a Compose surface can draw.
 *
 * Implemented by the desktop player, which renders libmpv frames into a bitmap.
 * Keeping this in the UI layer lets `VideoSurface` draw the video as ordinary
 * Compose content, so Compose controls can be layered on top of it (a native
 * heavyweight surface would cover them).
 */
interface FrameSource : VideoOutput {
    /** Bumped every time a new frame is ready to be drawn. */
    val frames: StateFlow<Long>

    /** Renders the current frame at the given pixel size, or `null` when not ready. */
    fun render(
        width: Int,
        height: Int,
    ): ImageBitmap?
}
