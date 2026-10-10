package com.misgus.agustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption

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
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val borderColor by animateColorAsState(
        targetValue = if (hovered) AppColors.accent else AppColors.divider,
    )
    val shape = RoundedCornerShape(AppRadii.md)
    val fraction = progress?.fraction
    val inProgress = !watched && fraction != null && fraction > 0.02 && fraction < 0.95

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(interactionSource = interaction, indication = null, onClick = onTap)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, borderColor, shape)
                .clip(shape),
        ) {
            PosterImage(item.poster, Modifier.fillMaxSize())
            if (watched) {
                Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs)) { WatchedBadge() }
            }
            if (inProgress) {
                LinearProgressIndicator(
                    progress = { fraction!!.toFloat() },
                    color = AppColors.accent,
                    trackColor = Color.Black.copy(alpha = 0.54f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = item.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(MaterialTheme.typography.bodyLarge),
        )
        item.year?.let { year ->
            Text(
                text = year.toString(),
                style = PosterCaption.year(MaterialTheme.typography.bodyMedium),
            )
        }
    }
}
