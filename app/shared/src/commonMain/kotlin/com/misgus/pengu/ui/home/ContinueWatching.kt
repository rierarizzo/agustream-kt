package com.misgus.pengu.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.detail_episode_code
import com.misgus.pengu.ui.components.PosterImage
import com.misgus.pengu.ui.components.clickableSurface
import com.misgus.pengu.ui.components.motionBorder
import com.misgus.pengu.ui.components.rememberInteraction
import com.misgus.pengu.ui.theme.AppColors
import com.misgus.pengu.ui.theme.AppCorner
import com.misgus.pengu.ui.theme.AppSizes
import com.misgus.pengu.ui.theme.AppSpacing
import com.misgus.pengu.ui.theme.PosterCaption
import com.misgus.pengu.ui.theme.motionTween
import org.jetbrains.compose.resources.stringResource

/** Width and rail height of a 16:9 "Continue watching" card, smaller on a phone. */
internal val ContinueWidth = 336.dp
internal val CompactContinueWidth = 224.dp
internal val ContinueHeight = 248.dp
internal val CompactContinueHeight = 184.dp

/** One "Continue watching" card: a 16:9 still, a progress bar, title and episode. */
@Composable
internal fun ContinueCard(
    entry: ContinueEntry,
    width: Dp,
    onTap: () -> Unit,
) {
    val item = entry.item
    val progress = entry.progress
    val season = progress.season
    val episodeNumber = progress.episode
    val episode =
        if (season == null || episodeNumber == null) {
            null
        } else {
            stringResource(Res.string.detail_episode_code, season, episodeNumber)
        }
    val backdrop = item.background ?: item.poster

    val interaction = rememberInteraction()
    val borderColor =
        animateColorAsState(
            targetValue = if (interaction.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            animationSpec = motionTween(),
            label = "borderColor",
        )

    Column(
        modifier = Modifier.width(width).clickableSurface(interaction, onTap, indication = null),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .motionBorder(color = { borderColor.value }, width = AppSizes.borderWidth, cornerRadius = AppCorner.small)
                    .clip(MaterialTheme.shapes.small),
        ) {
            PosterImage(backdrop, Modifier.fillMaxSize())
            progress.fraction?.let { fraction ->
                LinearProgressIndicator(
                    progress = { fraction.toFloat() },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = AppColors.mediaScrim,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(AppSizes.progressBarHeight),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            item.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(),
        )
        episode?.let { Text(it, style = PosterCaption.meta()) }
    }
}
