package com.misgus.pengu.player.mpv

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

/**
 * libmpv render context using the software API (`MPV_RENDER_API_TYPE_SW`).
 *
 * mpv draws the current frame into a caller-owned buffer; the UI turns that
 * into a Compose bitmap, so the video stays ordinary Compose content and the
 * Compose controls can be layered on top of it.
 *
 * The software API is CPU-bound; it is the simplest render path and a stepping
 * stone to the OpenGL one.
 */
internal class MpvRenderContext private constructor(
    private val lib: LibMpv,
    private var context: Pointer?,
) {
    private var updateCallback: MpvRenderUpdateCallback? = null

    /** Registers [onFrame], invoked by mpv when a new frame should be drawn. */
    fun setUpdateCallback(onFrame: () -> Unit) {
        val callback = MpvRenderUpdateCallback { onFrame() }
        updateCallback = callback
        lib.mpvRenderContextSetUpdateCallback(requireContext(), callback, null)
    }

    /** Whether a new frame is ready (`MPV_RENDER_UPDATE_FRAME`). */
    fun update(): Long = lib.mpvRenderContextUpdate(requireContext())

    /** Renders the current frame into [buffer] as [format]. */
    fun render(
        width: Int,
        height: Int,
        format: String,
        stride: Int,
        buffer: Pointer,
    ): Int {
        val size =
            Memory(8).apply {
                setInt(0, width)
                setInt(4, height)
            }
        val formatMemory = Memory(format.length + 1L).apply { setString(0, format) }
        val strideMemory = Memory(8).apply { setLong(0, stride.toLong()) }
        val params = renderParams(5)
        params[0]
            .apply {
                type = MpvRenderParamType.SW_SIZE
                data = size
            }.write()
        params[1]
            .apply {
                type = MpvRenderParamType.SW_FORMAT
                data = formatMemory
            }.write()
        params[2]
            .apply {
                type = MpvRenderParamType.SW_STRIDE
                data = strideMemory
            }.write()
        params[3]
            .apply {
                type = MpvRenderParamType.SW_POINTER
                data = buffer
            }.write()
        params[4]
            .apply {
                type = 0
                data = null
            }.write()
        return lib.mpvRenderContextRender(requireContext(), params[0].pointer)
    }

    fun free() {
        updateCallback = null
        context?.let { lib.mpvRenderContextFree(it) }
        context = null
    }

    private fun requireContext(): Pointer = context ?: error("The mpv render context was freed")

    companion object {
        /** Creates a software render context for [mpvHandle], or `null` on failure. */
        fun create(
            lib: LibMpv,
            mpvHandle: Pointer,
        ): MpvRenderContext? {
            val apiType = Memory(4).apply { setString(0, "sw") }
            val params = renderParams(2)
            params[0]
                .apply {
                    type = MpvRenderParamType.API_TYPE
                    data = apiType
                }.write()
            params[1]
                .apply {
                    type = 0
                    data = null
                }.write()
            val reference = PointerByReference()
            val result = lib.mpvRenderContextCreate(reference, mpvHandle, params[0].pointer)
            val context = reference.value
            if (result < 0 || context == null) return null
            return MpvRenderContext(lib, context)
        }

        /** JNA returns `Structure[]`; the elements are the same class as the receiver. */
        @Suppress("UNCHECKED_CAST")
        private fun renderParams(count: Int): Array<MpvRenderParam> = MpvRenderParam().toArray(count) as Array<MpvRenderParam>
    }
}
