package com.misgus.agustream.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.MediaTitle
import com.misgus.agustream.ui.components.MetadataRow
import com.misgus.agustream.ui.components.PillButton
import com.misgus.agustream.ui.components.ratingText
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppElevation
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing

/** Billboard crop ratio (Netflix-style wide banner). */
private const val HERO_ASPECT_RATIO = 1920f / 823f

/** Featured billboard carousel. */
@Composable
internal fun Hero(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(HERO_ASPECT_RATIO)
                    .shadow(elevation = AppElevation.hero, shape = AppRadii.xlShape, clip = false)
                    .clip(AppRadii.xlShape)
                    .border(1.dp, Color.White.copy(alpha = AppAlpha.HERO_RIM), AppRadii.xlShape),
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
            ) { page ->
                HeroSlide(
                    item = items[page],
                    onOpen = { onOpen(items[page]) },
                    onPlay = { onPlay(items[page]) },
                )
            }
            if (items.size > 1) {
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(AppSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    items.indices.forEach { index ->
                        val selected = index == pagerState.currentPage
                        Box(
                            modifier =
                                Modifier
                                    .width(if (selected) 20.dp else 6.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (selected) {
                                            Color.White
                                        } else {
                                            Color.White.copy(alpha = AppAlpha.HERO_INDICATOR)
                                        },
                                    ),
                        )
                    }
                }
            }
        }
    }
}

/** One billboard slide: full-bleed artwork with the copy and actions over a scrim. */
@Composable
private fun HeroSlide(
    item: MetaPreview,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    val backdrop = item.background ?: item.poster
    Box(Modifier.fillMaxSize().background(AppColors.surface)) {
        if (!backdrop.isNullOrEmpty()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Netflix-style scrim: dark on the left column and along the bottom, so
        // the copy stays readable no matter how bright the artwork is.
        Box(Modifier.fillMaxSize().background(AppScrim.mediaHorizontal))
        Box(Modifier.fillMaxSize().background(AppScrim.mediaBottom))
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.xl)
                    .widthIn(max = 620.dp),
        ) {
            MediaTitle(item.name, item.logo)
            MetadataRow(heroMetadataParts(item))
            item.description?.takeIf { it.isNotEmpty() }?.let {
                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    it,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textPrimary.copy(alpha = 0.92f),
                )
            }
            Spacer(Modifier.height(AppSpacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                PillButton(text = "Play", icon = Icons.Default.PlayArrow, onClick = onPlay)
                PillButton(text = "More info", icon = Icons.Outlined.Info, onClick = onOpen, primary = false)
            }
        }
    }
}

/** Type, genres, year and rating, dot-separated like the Netflix billboard. */
private fun heroMetadataParts(item: MetaPreview): List<String> {
    val type = item.type.replaceFirstChar { it.uppercase() }
    return buildList {
        if (type.isNotEmpty()) add(type)
        addAll(item.genres.take(2))
        yearOf(item.releaseInfo)?.let { add(it) }
        item.imdbRating?.let { add("★ ${ratingText(it)}") }
    }
}
