package com.misgus.pengu.player.mpv

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.PointerByReference

/**
 * Minimal JNA binding to libmpv's client API (`include/mpv/client.h`).
 *
 * Only the surface the player needs is declared: creation, options, commands,
 * property observation, the event loop and the software render context. Video
 * is produced through the render API so it can be drawn as Compose content.
 *
 * Methods are camelCase; [LibMpvLoader] installs a JNA function mapper that
 * turns each name into the matching `mpv_snake_case` symbol.
 */
internal interface LibMpv : Library {
    fun mpvCreate(): Pointer?

    fun mpvInitialize(ctx: Pointer): Int

    fun mpvSetOptionString(
        ctx: Pointer,
        name: String,
        data: String,
    ): Int

    fun mpvSetOption(
        ctx: Pointer,
        name: String,
        format: Int,
        data: Pointer,
    ): Int

    fun mpvCommand(
        ctx: Pointer,
        args: Pointer,
    ): Int

    fun mpvSetPropertyString(
        ctx: Pointer,
        name: String,
        data: String,
    ): Int

    fun mpvGetProperty(
        ctx: Pointer,
        name: String,
        format: Int,
        data: Pointer,
    ): Int

    fun mpvObserveProperty(
        ctx: Pointer,
        replyUserdata: Long,
        name: String,
        format: Int,
    ): Int

    fun mpvWaitEvent(
        ctx: Pointer,
        timeout: Double,
    ): Pointer?

    fun mpvWakeup(ctx: Pointer)

    fun mpvTerminateDestroy(ctx: Pointer)

    fun mpvErrorString(error: Int): String?

    fun mpvFree(data: Pointer)

    fun mpvRenderContextCreate(
        res: PointerByReference,
        mpv: Pointer,
        params: Pointer,
    ): Int

    fun mpvRenderContextSetUpdateCallback(
        ctx: Pointer,
        callback: MpvRenderUpdateCallback,
        callbackCtx: Pointer?,
    )

    fun mpvRenderContextUpdate(ctx: Pointer): Long

    fun mpvRenderContextRender(
        ctx: Pointer,
        params: Pointer,
    ): Int

    fun mpvRenderContextFree(ctx: Pointer)
}

/** `mpv_format` values used by this binding. */
internal object MpvFormat {
    const val NONE = 0
    const val STRING = 1
    const val FLAG = 3
    const val INT64 = 4
    const val DOUBLE = 5
}

/** `mpv_event_id` values used by this binding. */
internal object MpvEventId {
    const val NONE = 0
    const val SHUTDOWN = 1
    const val END_FILE = 7
    const val FILE_LOADED = 8
    const val IDLE = 11
    const val PROPERTY_CHANGE = 22
}

/** `mpv_end_file_reason` values used by this binding. */
internal object MpvEndFileReason {
    const val EOF = 0
    const val STOP = 2
    const val QUIT = 3
    const val ERROR = 4
}

/** `mpv_render_param_type` values used by this binding. */
internal object MpvRenderParamType {
    const val API_TYPE = 1
    const val SW_SIZE = 17
    const val SW_FORMAT = 18
    const val SW_STRIDE = 19
    const val SW_POINTER = 20
}

/** Bit returned by `mpv_render_context_update` when a new frame is ready. */
const val MPV_RENDER_UPDATE_FRAME = 1L

/** `mpv_render_update_fn`: invoked when a new frame should be rendered. */
internal fun interface MpvRenderUpdateCallback : Callback {
    fun invoke(callbackCtx: Pointer?)
}

/** `mpv_render_param`: a `(type, data)` pair, terminated by `{0, null}`. */
@Structure.FieldOrder("type", "data")
internal class MpvRenderParam(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var type: Int = 0

    @JvmField
    var data: Pointer? = null

    init {
        if (pointer != null) read()
    }
}

/**
 * `mpv_event`.
 *
 * The field names are Kotlin-side only: JNA maps by declaration order, which
 * matches the C layout.
 */
@Structure.FieldOrder("eventId", "error", "replyUserdata", "data")
internal class MpvEvent(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var eventId: Int = 0

    @JvmField
    var error: Int = 0

    @JvmField
    var replyUserdata: Long = 0

    @JvmField
    var data: Pointer? = null

    init {
        if (pointer != null) read()
    }
}

/** `mpv_event_property`, reached through [MpvEvent.data] for property changes. */
@Structure.FieldOrder("name", "format", "data")
internal class MpvEventProperty(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var name: Pointer? = null

    @JvmField
    var format: Int = 0

    @JvmField
    var data: Pointer? = null

    init {
        if (pointer != null) read()
    }
}

/** `mpv_event_end_file`, reached through [MpvEvent.data] on [MpvEventId.END_FILE]. */
@Structure.FieldOrder("reason", "error", "playlistEntryId", "playlistInsertId", "playlistInsertNumEntries")
internal class MpvEventEndFile(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var reason: Int = 0

    @JvmField
    var error: Int = 0

    @JvmField
    var playlistEntryId: Long = 0

    @JvmField
    var playlistInsertId: Long = 0

    @JvmField
    var playlistInsertNumEntries: Int = 0

    init {
        if (pointer != null) read()
    }
}
