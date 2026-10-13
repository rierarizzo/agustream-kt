package com.misgus.agustream.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.motionLayer
import com.misgus.agustream.ui.theme.motionTween

/** Scale a poster grows to on hover or focus. */
private const val HOVER_SCALE = 1.04f

/**
 * Poster card for a catalog item ([MetaPreview]), used by Home rows.
 *
 * [width] is the fixed width for horizontal rows; when null the card fills its
 * parent (used inside a grid).
 */
@Composable
fun MetaPosterCard(
    preview: MetaPreview,
    watched: Boolean = false,
    width: Dp? = null,
    onTap: () -> Unit,
) {
    val shape = MaterialTheme.shapes.small
    val interaction = rememberInteraction()
    val borderColor =
        animateColorAsState(
            targetValue = if (interaction.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            animationSpec = motionTween(),
            label = "borderColor",
        )
    val scale =
        animateFloatAsState(
            targetValue = if (interaction.active) HOVER_SCALE else 1f,
            animationSpec = motionTween(durationMillis = AppMotion.FAST),
            label = "posterScale",
        )
    Column(
        modifier =
            Modifier
                .then(if (width != null) Modifier.width(width) else Modifier)
                .fillMaxHeight()
                .clickableSurface(interaction, onTap, indication = null),
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
            PosterImage(preview.poster, Modifier.fillMaxWidth().fillMaxHeight())
            if (watched) {
                Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xs)) { WatchedBadge() }
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        MarqueeText(
            text = preview.name,
            style = PosterCaption.title(),
            active = interaction.active,
        )
        val releaseInfo = preview.releaseInfo
        if (!releaseInfo.isNullOrEmpty()) {
            Text(
                text = releaseInfo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = PosterCaption.meta(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
