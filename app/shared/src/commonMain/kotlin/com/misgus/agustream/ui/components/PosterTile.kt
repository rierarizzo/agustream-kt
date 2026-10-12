package com.misgus.agustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.motionLayer
import com.misgus.agustream.ui.theme.motionTween

/** Scale a poster grows to on hover or focus. */
private const val HOVER_SCALE = 1.04f

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
    val borderColor by animateColorAsState(
        targetValue = if (interaction.active) AppColors.accent else AppColors.divider,
    )
    val shape = RoundedCornerShape(AppRadii.md)
    val fraction = progress?.fraction
    val inProgress = !watched && fraction != null && fraction > 0.02 && fraction < 0.95
    val scale by animateFloatAsState(
        targetValue = if (interaction.active) HOVER_SCALE else 1f,
        animationSpec = motionTween(durationMillis = AppMotion.FAST),
        label = "posterScale",
    )

    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .clickableSurface(interaction, onTap)
                .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .motionLayer(scale = scale)
                    .border(1.dp, borderColor, shape)
                    .clip(shape),
        ) {
            PosterImage(item.poster, Modifier.fillMaxSize())
            if (watched) {
                Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs)) { WatchedBadge() }
            }
            if (inProgress) {
                LinearProgressIndicator(
                    progress = { fraction.toFloat() },
                    color = AppColors.accent,
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
            )
        }
    }
}
