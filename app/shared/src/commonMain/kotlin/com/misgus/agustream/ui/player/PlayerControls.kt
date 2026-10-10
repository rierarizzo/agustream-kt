package com.misgus.agustream.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration

/**
 * Overlay controls for the player: back, title, seek bar, play/pause, time,
 * volume, subtitles, audio and fullscreen.
 *
 * Pure: it takes plain values and callbacks, so it can be rendered and tested
 * without a native player.
 */
@Composable
fun PlayerControls(
    title: String,
    playing: Boolean,
    position: Duration,
    duration: Duration,
    volume: Double,
    visible: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Duration) -> Unit,
    onSeekBy: (Duration) -> Unit,
    onVolumeChanged: (Double) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSubtitles: () -> Unit,
    onAudio: () -> Unit,
    onBack: () -> Unit,
) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f)
    Box(Modifier.fillMaxSize().alpha(alpha)) {
        TopBar(title, onBack, Modifier.align(Alignment.TopCenter))
        if (!playing) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.45f), androidx.compose.foundation.shape.CircleShape),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(40.dp))
            }
        }
        BottomBar(
            playing = playing,
            position = position,
            duration = duration,
            volume = volume,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onSeekBy = onSeekBy,
            onVolumeChanged = onVolumeChanged,
            onToggleFullscreen = onToggleFullscreen,
            onSubtitles = onSubtitles,
            onAudio = onAudio,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun BottomBar(
    playing: Boolean,
    position: Duration,
    duration: Duration,
    volume: Double,
    onPlayPause: () -> Unit,
    onSeek: (Duration) -> Unit,
    onSeekBy: (Duration) -> Unit,
    onVolumeChanged: (Double) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSubtitles: () -> Unit,
    onAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalMs = duration.inWholeMilliseconds
    val maxValue = if (totalMs <= 0) 1f else totalMs.toFloat()
    val value = position.inWholeMilliseconds.coerceIn(0, if (totalMs <= 0) 0 else totalMs).toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))),
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Slider(
            value = value,
            onValueChange = { onSeek(Duration.parse("${it.toLong()}ms")) },
            valueRange = 0f..maxValue,
            enabled = totalMs > 0,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = Color.White,
                )
            }
            IconButton(onClick = { onSeekBy(Duration.parse("-10s")) }) {
                Icon(Icons.Default.Replay10, contentDescription = "Back 10 seconds", tint = Color.White)
            }
            IconButton(onClick = { onSeekBy(Duration.parse("10s")) }) {
                Icon(Icons.Default.Forward10, contentDescription = "Forward 10 seconds", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "${format(position)} / ${format(duration)}",
                color = Color.White,
                fontSize = 13.sp,
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Box(Modifier.width(110.dp)) {
                Slider(
                    value = volume.coerceIn(0.0, 100.0).toFloat(),
                    onValueChange = { onVolumeChanged(it.toDouble()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White),
                )
            }
            IconButton(onClick = onSubtitles) {
                Icon(Icons.Outlined.Subtitles, contentDescription = "Subtitles", tint = Color.White)
            }
            IconButton(onClick = onAudio) {
                Icon(Icons.Outlined.Audiotrack, contentDescription = "Audio", tint = Color.White)
            }
            IconButton(onClick = onToggleFullscreen) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
            }
        }
    }
}

private fun format(value: Duration): String {
    val total = value.inWholeSeconds
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
}
