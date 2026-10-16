package com.misgus.pengu.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.pengu.core.player.MediaTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.mpv.MPVHandle
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.mpv.compose.MpvMediampPlayerSurface

/**
 * [PlayerService] backed by **mediamp's mpv backend**.
 *
 * mediamp renders the video into the Compose/Skia canvas (D3D11 on Windows,
 * OpenGL on Linux, Metal on macOS), so the video is ordinary Compose content and
 * the in-window Compose controls sit on top of it. The shared
 * [MediampPlayerAdapter] holds the contract logic; this class wires the mpv
 * engine: its surface, its `AudioLevelController` volume and the mpv command
 * that adds an external addon subtitle.
 */
class MediampPlayerService : MediampPlayerAdapter(MpvMediampPlayer(Any(), Dispatchers.Default)) {
    private val mpvPlayer: MpvMediampPlayer get() = player as MpvMediampPlayer

    override fun createVideoOutput(): VideoSurfaceRenderer =
        object : VideoSurfaceRenderer {
            @Composable
            override fun Draw(modifier: Modifier) {
                MpvMediampPlayerSurface(mpvPlayer, modifier)
            }
        }

    override fun onInit() {
        player.features[AudioLevelController]?.let { controller ->
            scope.launch {
                controller.volume.collect { v -> updateState { it.copy(volume = v * 100.0) } }
            }
        }
    }

    override fun setVolume(volume: Double) {
        val level = volume.coerceIn(0.0, 100.0)
        onMain { player.features[AudioLevelController]?.setVolume((level / 100.0).toFloat()) }
        updateState { it.copy(volume = level) }
    }

    override fun selectSubtitleTrack(track: MediaTrack?) {
        val uri = track?.uri
        if (track == null) {
            setExternalSubtitleId(null)
            onMain { (player.impl as? MPVHandle)?.setPropertyString("sid", "no") }
            return
        }
        if (!uri.isNullOrEmpty()) {
            // External addon subtitle: mpv-only, added on top of the loaded media.
            setExternalSubtitleId(track.id)
            onMain { (player.impl as? MPVHandle)?.command("sub-add", uri, "select") }
            updateState { it.copy(selectedSubtitleId = track.id) }
            return
        }
        setExternalSubtitleId(null)
        selectEmbeddedSubtitle(track)
    }
}
