package com.misgus.agustream.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalMotionEnabled
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.screenPadding
import kotlin.math.ceil

/** Poster card ratio, mirrored by the real grids. */
private const val POSTER_ASPECT = 0.58f

/** Static stops of the shimmer sweep, built once instead of on every frame. */
private val ShimmerColors = listOf(AppColors.surface, AppColors.surfaceHigh, AppColors.surface)

/**
 * Full-screen grid of poster skeletons for a loading library or catalog.
 *
 * The cell count is derived from the available space, so the placeholders always
 * cover the whole viewport instead of stopping after a fixed number of rows. One
 * shimmer transition drives every cell: the progress is a [State] read in the
 * draw phase, so the grid repaints without recomposing per frame.
 */
@Composable
fun SkeletonPosterGrid(
    posterSize: PosterSize,
    modifier: Modifier = Modifier,
) {
    val progress = rememberShimmerProgress()
    val windowSize = LocalWindowSize.current
    val cellWidth = posterSize.cellWidth(windowSize)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val columns = columnsThatFit(maxWidth, cellWidth)
        // `Adaptive` stretches the cells to fill the row, so measure with the real
        // cell width instead of the nominal one to know how tall a row is.
        val cell = (maxWidth - AppSpacing.xl * 2 - AppSpacing.md * (columns - 1)) / columns
        val rows = rowsThatFit(maxHeight, cell / POSTER_ASPECT)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(cellWidth),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(windowSize.screenPadding),
            verticalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
            horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
            userScrollEnabled = false,
        ) {
            items(columns * rows) {
                Box(Modifier.aspectRatio(POSTER_ASPECT)) {
                    SkeletonBox(progress = progress, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/** How many [cellWidth] columns fit in [width] once the grid padding is removed. */
private fun columnsThatFit(
    width: Dp,
    cellWidth: Dp,
): Int {
    val usable = width - AppSpacing.xl * 2 + AppSpacing.md
    return maxOf(1, (usable / (cellWidth + AppSpacing.md)).toInt())
}

/** How many [rowHeight] rows are needed to cover [height], including the spacing. */
private fun rowsThatFit(
    height: Dp,
    rowHeight: Dp,
): Int {
    val usable = height - AppSpacing.xl * 2 + AppSpacing.md
    return maxOf(1, ceil(usable / (rowHeight + AppSpacing.md)).toInt())
}

/** A placeholder block that shimmers while content loads. */
@Composable
fun SkeletonBox(
    progress: State<Float>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadii.md),
) {
    if (LocalMotionEnabled.current) {
        Box(
            modifier.clip(shape).drawBehind {
                val p = progress.value
                val width = size.width
                val brush =
                    Brush.linearGradient(
                        colors = ShimmerColors,
                        start = Offset(p * width, 0f),
                        end = Offset((p + 0.6f) * width, 0f),
                    )
                drawRect(brush)
            },
        )
    } else {
        Box(modifier.clip(shape).background(AppColors.surface))
    }
}

/** A single infinite 0..1 sweep shared by every skeleton on screen. */
@Composable
fun rememberShimmerProgress(): State<Float> {
    // Do not spin an infinite animation when motion is off.
    if (!LocalMotionEnabled.current) return remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "skeleton")
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
        label = "skeletonProgress",
    )
}
