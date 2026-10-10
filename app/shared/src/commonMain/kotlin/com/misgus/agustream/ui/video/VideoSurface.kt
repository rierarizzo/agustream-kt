package com.misgus.agustream.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.misgus.agustream.core.player.PlayerService

/**
 * Renders the player's video output.
 *
 * Platform-specific: on desktop it embeds the engine's AWT component; on other
 * targets it shows a placeholder until a native surface is wired up.
 */
@Composable
expect fun VideoSurface(
    player: PlayerService,
    modifier: Modifier = Modifier,
)

/** Fallback surface shown when the engine has no output. */
@Composable
internal fun EmptyVideoSurface(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.Black))
}
