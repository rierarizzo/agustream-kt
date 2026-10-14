package com.misgus.agustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.ProgressThresholds
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppScale
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.motionLayer
import com.misgus.agustream.ui.theme.motionTween

/**
 * One poster in the library grid.
 *
 * Shows the artwork, title, year and — when there is watch progress — either a
 * progress bar (started) or a check badge (finished).
 */
@Composable
fun PosterTile(
    item: LibraryItem,
    progress: WatchProgress? = null,
    watched: Boolean = false,
    onTap: () -> Unit,
) {
    val interaction = rememberInteraction()
    val borderColor =
        animateColorAsState(
            targetValue = if (interaction.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            animationSpec = motionTween(),
            label = "borderColor",
        )
    val shape = MaterialTheme.shapes.small
    val fraction = progress?.fraction
    val inProgress =
        !watched && fraction != null && fraction > ProgressThresholds.IN_PROGRESS_MIN && fraction < ProgressThresholds.IN_PROGRESS_MAX
    val scale =
        animateFloatAsState(
            targetValue = if (interaction.active) AppScale.HOVER else 1f,
            animationSpec = motionTween(durationMillis = AppMotion.FAST),
            label = "posterScale",
        )

    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .clickableSurface(interaction, onTap, indication = null)
                .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .motionLayer(scale = { scale.value })
                    .motionBorder(color = { borderColor.value }, width = 1.dp, cornerRadius = AppCorner.small)
                    .clip(shape),
        ) {
            PosterImage(item.poster, Modifier.fillMaxSize())
            if (watched) {
                Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs)) { WatchedBadge() }
            }
            if (inProgress) {
                LinearProgressIndicator(
                    progress = { fraction.toFloat() },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = AppColors.mediaScrim,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        MarqueeText(
            text = item.name,
            style = PosterCaption.title(),
            active = interaction.active,
        )
        item.year?.let { year ->
            Text(
                text = year.toString(),
                style = PosterCaption.meta(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
