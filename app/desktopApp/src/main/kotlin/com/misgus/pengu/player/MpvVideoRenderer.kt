package com.misgus.pengu.player

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.misgus.pengu.player.mpv.MpvRenderContext
import com.misgus.pengu.ui.video.FrameSource
import com.sun.jna.Memory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

/**
 * Turns libmpv's software-rendered frames into Compose bitmaps.
 *
 * The buffer is reused between frames; a fresh [Bitmap] is produced per frame so
 * Compose sees a new image and repaints.
 */
internal class MpvVideoRenderer : FrameSource {
    private val _frames = MutableStateFlow(0L)
    override val frames: StateFlow<Long> = _frames.asStateFlow()

    private var renderContext: MpvRenderContext? = null
    private var width = 0
    private var height = 0
    private var stride = 0
    private var buffer: Memory? = null

    fun attach(context: MpvRenderContext) {
        renderContext = context
        context.setUpdateCallback { _frames.update { it + 1 } }
        _frames.update { it + 1 }
    }

    fun detach() {
        renderContext?.free()
        renderContext = null
        buffer = null
        width = 0
        height = 0
        _frames.update { it + 1 }
    }

    override fun render(
        width: Int,
        height: Int,
    ): ImageBitmap? {
        val context = renderContext ?: return null
        if (width <= 0 || height <= 0) return null
        ensureBuffer(width, height)
        val target = buffer ?: return null
        if (context.render(width, height, FORMAT, stride, target) < 0) return null
        val bytes = target.getByteArray(0, stride * height)
        val info = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
        val bitmap = Bitmap()
        bitmap.installPixels(info, bytes, stride)
        return bitmap.asComposeImageBitmap()
    }

    private fun ensureBuffer(
        width: Int,
        height: Int,
    ) {
        if (width == this.width && height == this.height && buffer != null) return
        this.width = width
        this.height = height
        stride = ((width * BYTES_PER_PIXEL + ALIGNMENT - 1) / ALIGNMENT) * ALIGNMENT
        buffer = Memory(stride.toLong() * height)
    }

    private companion object {
        const val FORMAT = "bgra"
        const val BYTES_PER_PIXEL = 4
        const val ALIGNMENT = 64
    }
}
