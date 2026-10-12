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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.SkeletonBox
import com.misgus.agustream.ui.components.rememberShimmerProgress
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.railHeight

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
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md)
                .aspectRatio(HERO_ASPECT_RATIO),
        ) {
            SkeletonBox(progress, Modifier.fillMaxSize(), AppRadii.xlShape)
        }
        SkeletonRailHeader(progress)
        SkeletonRail(progress, CONTINUE_CARD_WIDTH, CONTINUE_CARD_WIDTH / (16f / 9f))
        repeat(2) {
            SkeletonRailHeader(progress)
            SkeletonRail(progress, posterSize.cellWidth, posterSize.railHeight)
        }
    }
}

@Composable
private fun SkeletonRailHeader(progress: State<Float>) {
    SkeletonBox(
        progress,
        Modifier
            .padding(start = AppSpacing.xl, top = AppSpacing.lg, bottom = AppSpacing.md)
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
        contentPadding = PaddingValues(horizontal = AppSpacing.xl),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        userScrollEnabled = false,
    ) {
        items(8) {
            SkeletonBox(progress, Modifier.width(cardWidth).height(cardHeight))
        }
    }
}
