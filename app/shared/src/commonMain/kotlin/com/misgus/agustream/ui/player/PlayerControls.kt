package com.misgus.agustream.ui.player

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_back
import agustream.app.shared.generated.resources.action_play
import agustream.app.shared.generated.resources.player_audio
import agustream.app.shared.generated.resources.player_back_10
import agustream.app.shared.generated.resources.player_forward_10
import agustream.app.shared.generated.resources.player_fullscreen
import agustream.app.shared.generated.resources.player_pause
import agustream.app.shared.generated.resources.player_subtitles
import agustream.app.shared.generated.resources.player_volume
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.icons.AppIcons
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppIconSize
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.motionTween
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

/**
 * Read-only values the player controls render.
 *
 * Grouped so the composable takes one state plus callbacks instead of a long
 * list of positional values.
 */
data class PlayerControlsState(
    val title: String,
    val playing: Boolean,
    val position: Duration,
    val duration: Duration,
    val volume: Double,
    val visible: Boolean,
)

/**
 * Overlay controls for the player: back, title, seek bar, play/pause, time,
 * volume, subtitles, audio and fullscreen.
 *
 * Pure: it takes plain values and callbacks, so it can be rendered and tested
 * without a native player.
 */
@Composable
fun PlayerControls(
    state: PlayerControlsState,
    onPlayPause: () -> Unit,
    onSeek: (Duration) -> Unit,
    onSeekBy: (Duration) -> Unit,
    onVolumeChanged: (Double) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSubtitles: () -> Unit,
    onAudio: () -> Unit,
    onBack: () -> Unit,
) {
    val alpha by animateFloatAsState(if (state.visible) 1f else 0f, animationSpec = motionTween())
    Box(Modifier.fillMaxSize().alpha(alpha)) {
        TopBar(state.title, onBack, Modifier.align(Alignment.TopCenter))
        if (!state.playing) {
            IconButton(
                onClick = onPlayPause,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(AppColors.playerBackground.copy(alpha = AppAlpha.PLAYER_BUTTON), CircleShape),
            ) {
                Icon(
                    AppIcons.Filled.PlayArrow,
                    contentDescription = stringResource(Res.string.action_play),
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        BottomBar(
            state = state,
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
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    AppScrim.playerTop,
                ).statusBarsPadding()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                AppIcons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.action_back),
                tint = AppColors.playerControl,
            )
        }
        Spacer(Modifier.width(AppSpacing.xs))
        Text(
            title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.playerControl,
        )
    }
}

@Composable
private fun BottomBar(
    state: PlayerControlsState,
    onPlayPause: () -> Unit,
    onSeek: (Duration) -> Unit,
    onSeekBy: (Duration) -> Unit,
    onVolumeChanged: (Double) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSubtitles: () -> Unit,
    onAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalMs = state.duration.inWholeMilliseconds
    val maxValue = if (totalMs <= 0) 1f else totalMs.toFloat()
    val value =
        state.position.inWholeMilliseconds
            .coerceIn(0, if (totalMs <= 0) 0 else totalMs)
            .toFloat()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    AppScrim.playerBottom,
                ).navigationBarsPadding()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    ) {
        Slider(
            value = value,
            onValueChange = { onSeek(Duration.parse("${it.toLong()}ms")) },
            valueRange = 0f..maxValue,
            enabled = totalMs > 0,
            colors = SliderDefaults.colors(thumbColor = AppColors.playerControl, activeTrackColor = AppColors.playerControl),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (state.playing) AppIcons.Filled.Pause else AppIcons.Filled.PlayArrow,
                    contentDescription =
                        if (state.playing) {
                            stringResource(
                                Res.string.player_pause,
                            )
                        } else {
                            stringResource(Res.string.action_play)
                        },
                    tint = AppColors.playerControl,
                )
            }
            IconButton(onClick = { onSeekBy(SEEK_BACK) }) {
                Icon(
                    AppIcons.Filled.Replay10,
                    contentDescription = stringResource(Res.string.player_back_10),
                    tint = AppColors.playerControl,
                )
            }
            IconButton(onClick = { onSeekBy(SEEK_FORWARD) }) {
                Icon(
                    AppIcons.Filled.Forward10,
                    contentDescription = stringResource(Res.string.player_forward_10),
                    tint = AppColors.playerControl,
                )
            }
            Spacer(Modifier.width(AppSpacing.sm))
            Text(
                "${format(state.position)} / ${format(state.duration)}",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.playerControl,
            )
            Spacer(Modifier.weight(1f))
            Icon(
                AppIcons.AutoMirrored.Filled.VolumeUp,
                contentDescription = stringResource(Res.string.player_volume),
                tint = AppColors.playerControl,
                modifier = Modifier.size(AppIconSize.md),
            )
            Box(Modifier.width(110.dp)) {
                Slider(
                    value = state.volume.coerceIn(0.0, 100.0).toFloat(),
                    onValueChange = { onVolumeChanged(it.toDouble()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = AppColors.playerControl, activeTrackColor = AppColors.playerControl),
                )
            }
            IconButton(onClick = onSubtitles) {
                Icon(
                    AppIcons.Outlined.Subtitles,
                    contentDescription = stringResource(Res.string.player_subtitles),
                    tint = AppColors.playerControl,
                )
            }
            IconButton(onClick = onAudio) {
                Icon(
                    AppIcons.Outlined.Audiotrack,
                    contentDescription = stringResource(Res.string.player_audio),
                    tint = AppColors.playerControl,
                )
            }
            IconButton(onClick = onToggleFullscreen) {
                Icon(
                    AppIcons.Filled.Fullscreen,
                    contentDescription = stringResource(Res.string.player_fullscreen),
                    tint = AppColors.playerControl,
                )
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
