package com.misgus.agustream.core.player

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/** A selectable audio or subtitle track. */
data class MediaTrack(
    val id: String,
    val title: String? = null,
    val language: String? = null,
    /** Subtitle file URL, when the track is an external addon subtitle. */
    val uri: String? = null,
    val isExternal: Boolean = false,
)

/** Immutable snapshot of the player state. */
data class PlayerState(
    val position: Duration = Duration.ZERO,
    val duration: Duration = Duration.ZERO,
    val playing: Boolean = false,
    val buffering: Boolean = false,
    val completed: Boolean = false,
    /** Volume, `0`–`100`. */
    val volume: Double = 100.0,
    /** Last engine error, or `null`. */
    val error: String? = null,
    val audioTracks: List<MediaTrack> = emptyList(),
    val subtitleTracks: List<MediaTrack> = emptyList(),
    val selectedAudioId: String? = null,
    /** `null` means subtitles off. */
    val selectedSubtitleId: String? = null,
)

/**
 * Wraps the native playback engine so no composable talks to it directly.
 *
 * The player is isolated behind this interface (see the architecture rules);
 * the desktop implementation lives in the composition root.
 */
interface PlayerService {
    val state: StateFlow<PlayerState>

    /**
     * Platform handle for the video surface (on desktop, an AWT `Component`),
     * or `null` while no engine is available.
     *
     * The UI casts it to the platform type in `jvmMain`, which keeps the UI
     * layer independent from the concrete engine.
     */
    val videoOutput: Any?

    /** Opens [target]. */
    fun open(target: PlaybackTarget)

    fun playOrPause()

    fun seek(position: Duration)

    fun seekBy(offset: Duration)

    fun setVolume(volume: Double)

    fun selectAudioTrack(track: MediaTrack)

    /** Pass `null` to turn subtitles off. */
    fun selectSubtitleTrack(track: MediaTrack?)

    /** Stops playback and clears the current media. */
    fun stop()

    fun dispose()
}
