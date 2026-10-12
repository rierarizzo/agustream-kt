package com.misgus.agustream.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.SkeletonBox
import com.misgus.agustream.ui.components.rememberShimmerProgress
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.railHeight
import com.misgus.agustream.ui.theme.screenPadding

/** Width of a "Continue watching" card, mirrored from the real rail. */
private val CONTINUE_CARD_WIDTH = 336.dp

/**
 * Home placeholder shown on the first load: the billboard, a continue-watching
 * rail and two catalog rails, so the layout does not jump when the data arrives.
 */
@Composable
internal fun HomeSkeleton(
    posterSize: PosterSize,
    modifier: Modifier = Modifier,
) {
    val progress = rememberShimmerProgress()
    val windowSize = LocalWindowSize.current
    // Mirrors the real billboard: full-bleed on a phone, an inset card otherwise.
    val compact = windowSize == WindowSize.COMPACT
    val heroHorizontal = if (compact) 0.dp else windowSize.screenPadding
    val heroVertical = if (compact) 0.dp else AppSpacing.md
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = heroHorizontal, vertical = heroVertical)
                .aspectRatio(if (compact) COMPACT_HERO_ASPECT_RATIO else HERO_ASPECT_RATIO),
        ) {
            SkeletonBox(progress, Modifier.fillMaxSize(), if (compact) RectangleShape else AppRadii.xlShape)
        }
        SkeletonRailHeader(progress)
        SkeletonRail(progress, CONTINUE_CARD_WIDTH, CONTINUE_CARD_WIDTH / (16f / 9f))
        repeat(2) {
            SkeletonRailHeader(progress)
            SkeletonRail(progress, posterSize.cellWidth(windowSize), posterSize.railHeight(windowSize))
        }
    }
}

@Composable
private fun SkeletonRailHeader(progress: State<Float>) {
    SkeletonBox(
        progress,
        Modifier
            .padding(start = LocalWindowSize.current.screenPadding, top = AppSpacing.lg, bottom = AppSpacing.md)
            .width(160.dp)
            .height(24.dp),
    )
}

@Composable
private fun SkeletonRail(
    progress: State<Float>,
    cardWidth: Dp,
    cardHeight: Dp,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(cardHeight),
        contentPadding = PaddingValues(horizontal = LocalWindowSize.current.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(LocalWindowSize.current.gridSpacing),
        userScrollEnabled = false,
    ) {
        items(8) {
            SkeletonBox(progress, Modifier.width(cardWidth).height(cardHeight))
        }
    }
}
