package com.misgus.agustream.player

import com.misgus.agustream.core.player.MediaTrack
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.player.PlayerService
import com.misgus.agustream.core.player.PlayerState
import com.misgus.agustream.player.mpv.LibMpv
import com.misgus.agustream.player.mpv.LibMpvLoader
import com.misgus.agustream.player.mpv.MpvEndFileReason
import com.misgus.agustream.player.mpv.MpvEvent
import com.misgus.agustream.player.mpv.MpvEventEndFile
import com.misgus.agustream.player.mpv.MpvEventId
import com.misgus.agustream.player.mpv.MpvEventProperty
import com.misgus.agustream.player.mpv.MpvFormat
import com.misgus.agustream.player.mpv.MpvNativePlayer
import com.misgus.agustream.player.mpv.httpHeaderOption
import com.misgus.agustream.player.mpv.parseMpvTracks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * [PlayerService] backed by **libmpv**, loaded in process through JNA.
 *
 * Video is produced with libmpv's render API and handed to the UI as Compose
 * bitmaps, so the video is ordinary Compose content and the Compose controls
 * stay on top of it (a native `wid` surface would cover them). On Windows the
 * `libmpv-2.dll` is bundled with the app; on Linux the system `libmpv.so` is
 * used. The library is loaded lazily, so a machine without it degrades to a
 * clear playback error instead of crashing.
 */
class MpvPlayerService internal constructor(
    private val loadLibrary: () -> LibMpv = { LibMpvLoader.load() },
) : PlayerService {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val renderer = MpvVideoRenderer()

    override val videoOutput: Any get() = renderer

    override val canPlay: Boolean = true

    private val pendingLock = Any()
    private var pendingTarget: PlaybackTarget? = null

    private val startLock = Any()
    private val generation = AtomicLong(0)

    @Volatile
    private var starting = false

    @Volatile
    private var started = false

    @Volatile
    private var failed = false

    @Volatile
    private var player: MpvNativePlayer? = null

    @Volatile
    private var externalSubtitleId: String? = null

    override fun open(target: PlaybackTarget) {
        externalSubtitleId = null
        _state.value = PlayerState()
        synchronized(pendingLock) { pendingTarget = target }
        if (started) loadPending() else ensureStartedAsync()
    }

    override fun playOrPause() {
        player?.command("cycle", "pause")
    }

    override fun seek(position: Duration) {
        player?.command("seek", seconds(position), "absolute")
    }

    override fun seekBy(offset: Duration) {
        player?.command("seek", seconds(offset), "relative")
    }

    override fun setVolume(volume: Double) {
        val level = volume.coerceIn(0.0, 100.0)
        player?.setProperty("volume", level.toString())
        _state.update { it.copy(volume = level) }
    }

    override fun selectAudioTrack(track: MediaTrack) {
        val id = track.id.toIntOrNull() ?: return
        player?.setProperty("aid", id.toString())
    }

    override fun selectSubtitleTrack(track: MediaTrack?) {
        if (track == null) {
            externalSubtitleId = null
            player?.setProperty("sid", "no")
            return
        }
        val uri = track.uri
        if (!uri.isNullOrEmpty()) {
            externalSubtitleId = track.id
            player?.command("sub-add", uri, "select")
            return
        }
        val id = track.id.toIntOrNull() ?: return
        externalSubtitleId = null
        player?.setProperty("sid", id.toString())
    }

    override fun stop() {
        externalSubtitleId = null
        player?.command("stop")
        _state.value = PlayerState()
    }

    override fun dispose() {
        shutdownEngine()
        synchronized(pendingLock) { pendingTarget = null }
        _state.value = PlayerState()
    }

    /** Starts libmpv in the background. */
    private fun ensureStartedAsync() {
        val gen: Long
        synchronized(startLock) {
            if (started || starting || failed) return
            starting = true
            gen = generation.get()
        }
        thread(isDaemon = true, name = "mpv-start") {
            val ok = startEngine(gen)
            synchronized(startLock) {
                if (gen == generation.get()) starting = false
            }
            if (ok) loadPending()
        }
    }

    private fun startEngine(gen: Long): Boolean {
        if (gen != generation.get() || started) return false
        val lib =
            try {
                loadLibrary()
            } catch (error: Throwable) {
                failed = true
                setError(error.message ?: "libmpv could not be loaded.")
                return false
            }
        val native = MpvNativePlayer(lib)
        if (!native.start(::onEvent)) {
            failed = true
            setError("libmpv could not be initialized.")
            return false
        }
        val renderContext = native.createRenderContext()
        if (renderContext == null) {
            native.destroy()
            failed = true
            setError("libmpv could not create a render context.")
            return false
        }
        if (gen != generation.get()) {
            renderContext.free()
            native.destroy()
            return false
        }
        renderer.attach(renderContext)
        player = native
        started = true
        observeProperties(native)
        return true
    }

    private fun shutdownEngine() {
        generation.incrementAndGet()
        started = false
        starting = false
        failed = false
        renderer.detach()
        val native = player
        player = null
        native?.destroy()
    }

    private fun observeProperties(native: MpvNativePlayer) {
        native.observe("time-pos", MpvFormat.DOUBLE, 1)
        native.observe("duration", MpvFormat.DOUBLE, 2)
        native.observe("pause", MpvFormat.FLAG, 3)
        native.observe("paused-for-cache", MpvFormat.FLAG, 4)
        native.observe("eof-reached", MpvFormat.FLAG, 5)
        native.observe("volume", MpvFormat.DOUBLE, 6)
        native.observe("track-list", MpvFormat.STRING, 7)
        native.observe("idle-active", MpvFormat.FLAG, 8)
    }

    private fun loadPending() {
        val target =
            synchronized(pendingLock) {
                val value = pendingTarget
                pendingTarget = null
                value
            } ?: return
        val native = player ?: return
        httpHeaderOption(target.httpHeaders)?.let { native.setProperty("http-header-fields", it) }
        native.command("loadfile", target.source, "replace")
    }

    private fun onEvent(event: MpvEvent) {
        when (event.eventId) {
            MpvEventId.PROPERTY_CHANGE -> onPropertyChange(event)
            MpvEventId.END_FILE ->
                event.data?.let { pointer ->
                    if (MpvEventEndFile(pointer).reason == MpvEndFileReason.EOF) {
                        _state.update { it.copy(playing = false, completed = true) }
                    }
                }
            MpvEventId.FILE_LOADED -> _state.update { it.copy(completed = false) }
            MpvEventId.IDLE -> _state.update { it.copy(playing = false) }
        }
    }

    private fun onPropertyChange(event: MpvEvent) {
        val pointer = event.data ?: return
        val property = MpvEventProperty(pointer)
        val name = property.name?.getString(0) ?: return
        val data = property.data
        when (name) {
            "time-pos" ->
                if (property.format == MpvFormat.DOUBLE && data != null) {
                    _state.update { it.copy(position = data.getDouble(0).seconds) }
                }
            "duration" ->
                if (property.format == MpvFormat.DOUBLE && data != null) {
                    _state.update { it.copy(duration = data.getDouble(0).seconds) }
                }
            "pause" ->
                if (property.format == MpvFormat.FLAG && data != null) {
                    _state.update { it.copy(playing = data.getInt(0) == 0) }
                }
            "paused-for-cache" ->
                if (property.format == MpvFormat.FLAG && data != null) {
                    _state.update { it.copy(buffering = data.getInt(0) != 0) }
                }
            "eof-reached" ->
                if (property.format == MpvFormat.FLAG && data != null && data.getInt(0) != 0) {
                    _state.update { it.copy(completed = true, playing = false) }
                }
            "volume" ->
                if (property.format == MpvFormat.DOUBLE && data != null) {
                    _state.update { it.copy(volume = data.getDouble(0).coerceIn(0.0, 100.0)) }
                }
            "track-list" -> updateTracks(property)
            "idle-active" ->
                if (property.format == MpvFormat.FLAG && data != null && data.getInt(0) != 0) {
                    _state.update { it.copy(playing = false) }
                }
        }
    }

    private fun updateTracks(property: MpvEventProperty) {
        val json =
            if (property.format == MpvFormat.STRING) {
                property.data?.getPointer(0)?.getString(0)
            } else {
                null
            }
        val tracks = parseMpvTracks(json)
        _state.update {
            it.copy(
                audioTracks = tracks.audio,
                subtitleTracks = tracks.subtitles,
                selectedAudioId = tracks.selectedAudioId,
                selectedSubtitleId = resolveSubtitleId(tracks.selectedSubtitleId, tracks.subtitles),
            )
        }
    }

    /**
     * Keeps an external addon subtitle selected by its URL, matching what the
     * track menu expects, instead of the numeric id mpv assigned to it.
     */
    private fun resolveSubtitleId(
        mpvId: String?,
        tracks: List<MediaTrack> = _state.value.subtitleTracks,
    ): String? {
        val url = externalSubtitleId ?: return mpvId
        return if (tracks.any { it.uri == url }) url else mpvId
    }

    private fun setError(message: String) {
        _state.update { it.copy(error = message) }
    }

    private fun seconds(value: Duration): String = (value.inWholeMilliseconds / 1000.0).toString()
}
