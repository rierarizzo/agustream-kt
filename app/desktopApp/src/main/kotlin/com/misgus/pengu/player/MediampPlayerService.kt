package com.misgus.pengu.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.core.player.MediaTrack
import com.misgus.pengu.core.player.PlaybackTarget
import com.misgus.pengu.core.player.PlayerService
import com.misgus.pengu.core.player.PlayerState
import com.misgus.pengu.core.player.VideoOutput
import com.misgus.pengu.ui.video.VideoSurfaceRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openani.mediamp.MediaStatus
import org.openani.mediamp.errorOrNull
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.MediaMetadata
import org.openani.mediamp.metadata.AudioTrack
import org.openani.mediamp.metadata.SubtitleTrack
import org.openani.mediamp.mpv.MPVHandle
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.mpv.compose.MpvMediampPlayerSurface
import org.openani.mediamp.source.MediaExtraFiles
import org.openani.mediamp.source.UriMediaData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * [PlayerService] backed by **mediamp's mpv backend**.
 *
 * mediamp renders the video into the Compose/Skia canvas (D3D11 on Windows,
 * OpenGL on Linux, Metal on macOS), so the video is ordinary Compose content and
 * the in-window Compose controls sit on top of it. This adapter keeps the app's
 * [PlayerService] contract, so the UI stays independent from mediamp.
 */
class MediampPlayerService : PlayerService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val player: MpvMediampPlayer = MpvMediampPlayer(Any(), Dispatchers.Default)

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    // Track groups expose `candidates` as a plain Flow, so the latest emission is
    // kept to resolve a selection back to the candidate object.
    private var audioCandidates: List<AudioTrack> = emptyList()
    private var subtitleCandidates: List<SubtitleTrack> = emptyList()

    private var externalSubtitleId: String? = null

    override val videoOutput: VideoOutput =
        object : VideoSurfaceRenderer {
            @Composable
            override fun Draw(modifier: Modifier) {
                MpvMediampPlayerSurface(player, modifier)
            }
        }

    override val canPlay: Boolean = true

    init {
        scope.launch {
            player.state.collect { s ->
                _state.update {
                    it.copy(
                        playing = s.playWhenReady,
                        buffering = s.isBuffering || s.mediaStatus == MediaStatus.Opening,
                        completed = s.mediaStatus == MediaStatus.Ended,
                        error = s.errorOrNull?.message,
                    )
                }
            }
        }
        scope.launch {
            player.currentPositionMillis.collect { ms -> _state.update { it.copy(position = ms.milliseconds) } }
        }
        scope.launch {
            player.mediaProperties.collect { p ->
                _state.update { it.copy(duration = (p?.durationMillis ?: 0L).milliseconds) }
            }
        }
        player.features[AudioLevelController]?.let { controller ->
            scope.launch {
                controller.volume.collect { v -> _state.update { it.copy(volume = (v * 100.0).toDouble()) } }
            }
        }
        player.features[MediaMetadata]?.let { metadata ->
            metadata.audioTracks?.let { group ->
                scope.launch {
                    group.candidates.collect { list ->
                        audioCandidates = list
                        _state.update { it.copy(audioTracks = list.map(AudioTrack::toMediaTrack)) }
                    }
                }
                scope.launch { group.selected.collect { sel -> _state.update { it.copy(selectedAudioId = sel?.id) } } }
            }
            metadata.subtitleTracks?.let { group ->
                scope.launch {
                    group.candidates.collect { list ->
                        subtitleCandidates = list
                        _state.update { it.copy(subtitleTracks = list.map(SubtitleTrack::toMediaTrack)) }
                    }
                }
                scope.launch {
                    group.selected.collect { sel ->
                        _state.update { it.copy(selectedSubtitleId = externalSubtitleId ?: sel?.id) }
                    }
                }
            }
        }
    }

    override fun open(target: PlaybackTarget) {
        externalSubtitleId = null
        _state.value = PlayerState()
        onMain {
            runCatching {
                player.setMediaData(
                    UriMediaData(target.source, target.httpHeaders ?: emptyMap(), MediaExtraFiles.EMPTY),
                    playWhenReady = true,
                )
            }.onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Could not open the source.") }
            }
        }
    }

    override fun playOrPause() =
        onMain {
            if (player.state.value.playWhenReady) player.pause() else player.play()
        }

    override fun seek(position: Duration) = onMain { player.seekTo(position.inWholeMilliseconds) }

    override fun seekBy(offset: Duration) = onMain { player.skip(offset.inWholeMilliseconds) }

    override fun setVolume(volume: Double) {
        val level = volume.coerceIn(0.0, 100.0)
        onMain { player.features[AudioLevelController]?.setVolume((level / 100.0).toFloat()) }
        _state.update { it.copy(volume = level) }
    }

    override fun selectAudioTrack(track: MediaTrack) =
        onMain {
            val candidate = audioCandidates.firstOrNull { it.id == track.id } ?: return@onMain
            player.features[MediaMetadata]?.audioTracks?.select(candidate)
        }

    override fun selectSubtitleTrack(track: MediaTrack?) =
        onMain {
            val uri = track?.uri
            if (track == null) {
                externalSubtitleId = null
                (player.impl as? MPVHandle)?.setPropertyString("sid", "no")
                return@onMain
            }
            if (!uri.isNullOrEmpty()) {
                // External addon subtitle: mpv-only, added on top of the loaded media.
                externalSubtitleId = track.id
                (player.impl as? MPVHandle)?.command("sub-add", uri, "select")
                _state.update { it.copy(selectedSubtitleId = track.id) }
                return@onMain
            }
            externalSubtitleId = null
            val candidate = subtitleCandidates.firstOrNull { it.id == track.id } ?: return@onMain
            player.features[MediaMetadata]?.subtitleTracks?.select(candidate)
        }

    override fun stop() {
        externalSubtitleId = null
        onMain { player.stopPlayback() }
        _state.value = PlayerState()
    }

    override fun dispose() {
        externalSubtitleId = null
        onMain {
            runCatching { player.close() }
            scope.cancel()
        }
        _state.value = PlayerState()
    }

    /** Every command must run on the player's main dispatcher (the Swing EDT). */
    private fun onMain(block: suspend () -> Unit) {
        scope.launch(player.mainDispatcher) { block() }
    }
}

private fun AudioTrack.toMediaTrack(): MediaTrack =
    MediaTrack(
        id = id,
        title = name ?: labels.firstOrNull()?.value,
        language = labels.firstOrNull()?.language,
    )

private fun SubtitleTrack.toMediaTrack(): MediaTrack =
    MediaTrack(
        id = id,
        title = labels.firstOrNull()?.value,
        language = language,
    )
