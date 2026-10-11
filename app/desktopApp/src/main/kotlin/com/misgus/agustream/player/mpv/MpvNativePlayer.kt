package com.misgus.agustream.player.mpv

import com.sun.jna.Pointer
import com.sun.jna.StringArray
import com.sun.jna.ptr.PointerByReference
import kotlin.concurrent.thread

/**
 * One libmpv handle.
 *
 * Owns the event loop thread and translates the calls the player service needs.
 * Video is produced through a [MpvRenderContext] rather than a native window.
 */
internal class MpvNativePlayer(
    private val lib: LibMpv,
) {
    private var handle: Pointer? = null
    private var eventThread: Thread? = null

    @Volatile
    private var running = false

    /** Creates and initializes the mpv handle. */
    fun start(onEvent: (MpvEvent) -> Unit): Boolean {
        val ctx = lib.mpvCreate() ?: return false
        lib.mpvSetOptionString(ctx, "idle", "yes")
        lib.mpvSetOptionString(ctx, "terminal", "no")
        lib.mpvSetOptionString(ctx, "osc", "no")
        lib.mpvSetOptionString(ctx, "input-default-bindings", "no")
        lib.mpvSetOptionString(ctx, "hwdec", "auto-safe")
        lib.mpvSetOptionString(ctx, "keep-open", "no")

        if (lib.mpvInitialize(ctx) < 0) {
            lib.mpvTerminateDestroy(ctx)
            return false
        }
        handle = ctx
        running = true
        eventThread =
            thread(isDaemon = true, name = "mpv-events") {
                eventLoop(ctx, onEvent)
            }
        return true
    }

    /** Creates a software render context for this handle, or `null` on failure. */
    fun createRenderContext(): MpvRenderContext? {
        val ctx = handle ?: return null
        return MpvRenderContext.create(lib, ctx)
    }

    fun command(vararg args: String): Int {
        val ctx = handle ?: return -1
        return lib.mpvCommand(ctx, StringArray(args))
    }

    fun setProperty(
        name: String,
        value: String,
    ): Int {
        val ctx = handle ?: return -1
        return lib.mpvSetPropertyString(ctx, name, value)
    }

    /** Reads a property as its string form, or `null` when unavailable. */
    fun getPropertyString(name: String): String? {
        val ctx = handle ?: return null
        val reference = PointerByReference()
        if (lib.mpvGetProperty(ctx, name, MpvFormat.STRING, reference.pointer) < 0) return null
        val value = reference.value ?: return null
        return try {
            value.getString(0)
        } finally {
            lib.mpvFree(value)
        }
    }

    fun observe(
        name: String,
        format: Int,
        userdata: Long,
    ): Int {
        val ctx = handle ?: return -1
        return lib.mpvObserveProperty(ctx, userdata, name, format)
    }

    fun errorString(code: Int): String = lib.mpvErrorString(code) ?: "error $code"

    fun destroy() {
        val ctx = handle ?: return
        running = false
        lib.mpvWakeup(ctx)
        eventThread?.join(SHUTDOWN_JOIN_MILLIS)
        eventThread = null
        handle = null
        lib.mpvTerminateDestroy(ctx)
    }

    private fun eventLoop(
        ctx: Pointer,
        onEvent: (MpvEvent) -> Unit,
    ) {
        while (running) {
            val pointer = lib.mpvWaitEvent(ctx, -1.0) ?: break
            val event = MpvEvent(pointer)
            if (event.eventId == MpvEventId.NONE) continue
            onEvent(event)
            if (event.eventId == MpvEventId.SHUTDOWN) break
        }
    }

    private companion object {
        const val SHUTDOWN_JOIN_MILLIS = 2_000L
    }
}
