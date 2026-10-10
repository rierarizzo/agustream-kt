package com.misgus.agustream.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import com.misgus.agustream.core.player.PlayerService
import java.awt.Component

/**
 * Embeds the engine's AWT component (VLCJ's `EmbeddedMediaPlayerComponent`) into
 * the Compose scene.
 *
 * The cast to [Component] is deliberate: it keeps this UI module independent
 * from the concrete player implementation, which only exposes `Any?`.
 */
@Composable
actual fun VideoSurface(player: PlayerService, modifier: Modifier) {
    val component = player.videoOutput as? Component
    if (component == null) {
        EmptyVideoSurface(modifier)
        return
    }
    SwingPanel(
        background = Color.Black,
        modifier = modifier,
        factory = { component },
    )
}
