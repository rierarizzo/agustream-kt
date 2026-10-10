package com.misgus.agustream.player

import com.misgus.agustream.core.player.MediaTrack
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.player.PlayerService
import com.misgus.agustream.core.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import javax.swing.SwingUtilities
import kotlin.time.Duration.Companion.milliseconds

/**
 * [PlayerService] backed by libvlc through VLCJ.
 *
 * The engine is created lazily and only when VLC is discoverable, so a machine
 * without VLC degrades to a clear playback error instead of crashing. This is
 * the only class in the desktop app that depends on VLCJ.
 */
class VlcjPlayerService : PlayerService {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var initialized = false
    private var engine: Engine? = null
    private var externalSubtitleId: String? = null

    override val videoOutput: Any?
        get() = engineOrNull()?.component

    override fun open(target: PlaybackTarget) {
        val engine = engineOrNull()
        if (engine == null) {
            _state.update { it.copy(error = "No playback engine available (libvlc not found).") }
            return
        }
        externalSubtitleId = null
        _state.value = PlayerState()
        val options = buildMediaOptions(target)
        runCatching { engine.mediaPlayer.media().play(target.source, *options) }
            .onFailure { error ->
                _state.update { state ->
                    state.copy(error = error.message ?: "Playback failed.")
                }
            }
        refreshTracks()
    }

    override fun playOrPause() {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        val playing = mediaPlayer.status().isPlaying()
        mediaPlayer.controls().setPause(playing)
    }

    override fun seek(position: kotlin.time.Duration) {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        mediaPlayer.controls().setTime(position.inWholeMilliseconds)
    }

    override fun seekBy(offset: kotlin.time.Duration) {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        mediaPlayer.controls().skipTime(offset.inWholeMilliseconds)
    }

    override fun setVolume(volume: Double) {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        mediaPlayer.audio().setVolume(volume.toInt())
        _state.update { it.copy(volume = volume) }
    }

    override fun selectAudioTrack(track: MediaTrack) {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        val id = track.id.removePrefix(AUDIO_PREFIX).toIntOrNull() ?: return
        mediaPlayer.audio().setTrack(id)
        refreshTracks()
    }

    override fun selectSubtitleTrack(track: MediaTrack?) {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        if (track == null) {
            externalSubtitleId = null
            mediaPlayer.subpictures().setTrack(-1)
        } else if (!track.uri.isNullOrEmpty()) {
            externalSubtitleId = track.id
            mediaPlayer.subpictures().setSubTitleUri(track.uri)
        } else {
            externalSubtitleId = null
            val id = track.id.removePrefix(SUB_PREFIX).toIntOrNull() ?: return
            mediaPlayer.subpictures().setTrack(id)
        }
        refreshTracks()
    }

    override fun stop() {
        engineOrNull()?.mediaPlayer?.controls()?.stop()
        _state.value = PlayerState()
    }

    override fun dispose() {
        engine?.let { runCatching { it.component.release() } }
        engine = null
        initialized = false
    }

    private fun buildMediaOptions(target: PlaybackTarget): Array<String> =
        buildList {
            target.httpHeaders?.forEach { (key, value) ->
                when (key.lowercase()) {
                    "referer", "referrer" -> add(":http-referrer=$value")
                    "user-agent" -> add(":http-user-agent=$value")
                    else -> add(":http-header=$key: $value")
                }
            }
        }.toTypedArray()

    private fun refreshTracks() {
        val mediaPlayer = engineOrNull()?.mediaPlayer ?: return
        val audioTracks =
            runCatching {
                mediaPlayer
                    .audio()
                    .trackDescriptions()
                    .filter { it.id() >= 0 }
                    .map { MediaTrack(id = "$AUDIO_PREFIX${it.id()}", title = it.description()) }
            }.getOrDefault(emptyList())
        val subtitleTracks =
            runCatching {
                mediaPlayer
                    .subpictures()
                    .trackDescriptions()
                    .filter { it.id() >= 0 }
                    .map { MediaTrack(id = "$SUB_PREFIX${it.id()}", title = it.description()) }
            }.getOrDefault(emptyList())
        val selectedAudio = runCatching { mediaPlayer.audio().track() }.getOrDefault(-1)
        val selectedSub = runCatching { mediaPlayer.subpictures().track() }.getOrDefault(-1)
        _state.update {
            it.copy(
                audioTracks = audioTracks,
                subtitleTracks = subtitleTracks,
                selectedAudioId = if (selectedAudio >= 0) "$AUDIO_PREFIX$selectedAudio" else null,
                selectedSubtitleId =
                    externalSubtitleId
                        ?: if (selectedSub >= 0) "$SUB_PREFIX$selectedSub" else null,
                duration =
                    mediaPlayer
                        .status()
                        .length()
                        .coerceAtLeast(0)
                        .milliseconds,
            )
        }
    }

    private fun engineOrNull(): Engine? {
        if (initialized) return engine
        initialized = true
        engine =
            runCatching {
                // VLCJ expects native discovery before any player is created.
                if (!NativeDiscovery().discover()) return@runCatching null
                var created: EmbeddedMediaPlayerComponent? = null
                // Swing components must be built on the EDT.
                if (SwingUtilities.isEventDispatchThread()) {
                    created = EmbeddedMediaPlayerComponent()
                } else {
                    SwingUtilities.invokeAndWait { created = EmbeddedMediaPlayerComponent() }
                }
                val component = created ?: return@runCatching null
                val mediaPlayer = component.mediaPlayer()
                mediaPlayer.events().addMediaPlayerEventListener(listener())
                Engine(component, mediaPlayer)
            }.getOrNull()
        return engine
    }

    private fun listener() =
        object : MediaPlayerEventAdapter() {
            override fun timeChanged(
                mediaPlayer: MediaPlayer,
                newTime: Long,
            ) {
                _state.update { it.copy(position = newTime.coerceAtLeast(0).milliseconds) }
            }

            override fun lengthChanged(
                mediaPlayer: MediaPlayer,
                newLength: Long,
            ) {
                _state.update { it.copy(duration = newLength.coerceAtLeast(0).milliseconds) }
            }

            override fun playing(mediaPlayer: MediaPlayer) {
                _state.update { it.copy(playing = true, completed = false) }
                refreshTracks()
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                _state.update { it.copy(playing = false) }
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                _state.update { it.copy(playing = false) }
            }

            override fun finished(mediaPlayer: MediaPlayer) {
                _state.update { it.copy(playing = false, completed = true) }
            }

            override fun buffering(
                mediaPlayer: MediaPlayer,
                newCache: Float,
            ) {
                _state.update { it.copy(buffering = newCache < 100f) }
            }

            override fun volumeChanged(
                mediaPlayer: MediaPlayer,
                volume: Float,
            ) {
                _state.update { it.copy(volume = volume.toDouble()) }
            }

            override fun error(mediaPlayer: MediaPlayer) {
                _state.update { it.copy(error = "Playback failed.") }
            }

            override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
                refreshTracks()
            }

            override fun elementaryStreamAdded(
                mediaPlayer: MediaPlayer,
                trackType: uk.co.caprica.vlcj.media.TrackType,
                trackId: Int,
            ) {
                refreshTracks()
            }
        }

    private class Engine(
        val component: EmbeddedMediaPlayerComponent,
        val mediaPlayer: EmbeddedMediaPlayer,
    )

    private companion object {
        const val AUDIO_PREFIX = "audio:"
        const val SUB_PREFIX = "sub:"
    }
}
