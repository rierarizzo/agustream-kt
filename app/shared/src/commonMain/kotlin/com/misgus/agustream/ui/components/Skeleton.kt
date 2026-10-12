package com.misgus.agustream.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalMotionEnabled

/**
 * Full-screen grid of poster skeletons for a loading library or catalog.
 *
 * One shimmer transition drives every cell: a single infinite animation, read in
 * the draw phase, repaints without recomposing and stops with the loading screen.
 */
@Composable
fun SkeletonPosterGrid(
    modifier: Modifier = Modifier,
    count: Int = 18,
) {
    val progress = shimmerProgress()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(230.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        userScrollEnabled = false,
    ) {
        repeat(count) {
            item {
                Box(Modifier.aspectRatio(0.58f)) {
                    SkeletonBox(progress = progress, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/** A placeholder block that shimmers while content loads. */
@Composable
fun SkeletonBox(
    progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadii.md),
) {
    if (LocalMotionEnabled.current) {
        Box(
            modifier.clip(shape).drawBehind {
                val width = size.width
                val brush =
                    Brush.linearGradient(
                        colors = listOf(AppColors.surface, AppColors.surfaceHigh, AppColors.surface),
                        start = Offset(progress * width, 0f),
                        end = Offset((progress + 0.6f) * width, 0f),
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
private fun shimmerProgress(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
        label = "skeletonProgress",
    )
    return progress
}
