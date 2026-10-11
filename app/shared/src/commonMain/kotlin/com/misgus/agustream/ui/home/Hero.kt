package com.misgus.agustream.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.misgus.agustream.ui.shell.AmbientBackground
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppElevation
import com.misgus.agustream.ui.theme.AppRadii
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.ambientColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Billboard crop ratio (Netflix-style wide banner). */
private const val HERO_ASPECT_RATIO = 1920f / 823f

/**
 * Featured billboard carousel.
 *
 * Each slide also reports the dominant colour of its artwork, which is pushed to
 * [com.misgus.agustream.ui.shell.AppAmbient] so the whole app background can tint
 * along with it.
 */
@Composable
internal fun Hero(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val pageColors = remember { mutableStateMapOf<Int, Color>() }

    // The current slide's colour drives the app-wide background tint.
    AmbientBackground(pageColors[pagerState.currentPage])

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
                    onColor = { color -> pageColors[page] = color },
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
    onColor: (Color) -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    val backdrop = item.background ?: item.poster
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(AppColors.surface)) {
        if (!backdrop.isNullOrEmpty()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { state ->
                    scope.launch {
                        val color = withContext(Dispatchers.Default) { ambientColor(state.result.image) }
                        if (color != null) onColor(color)
                    }
                },
            )
        }
        // Netflix-style scrim: dark on the left column and along the bottom, so
        // the copy stays readable no matter how bright the artwork is.
        Box(Modifier.fillMaxSize().background(AppScrim.mediaHorizontal))
        Box(Modifier.fillMaxSize().background(AppScrim.heroVertical))
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.xl)
                    .widthIn(max = 620.dp),
        ) {
            HeroTitle(item)
            HeroMetadata(item)
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
                HeroButton(onClick = onPlay, primary = true) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("Play")
                }
                HeroButton(onClick = onOpen, primary = false) {
                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("More info")
                }
            }
        }
    }
}

/** Title logo when the metadata provides one, otherwise the name as a headline. */
@Composable
private fun HeroTitle(item: MetaPreview) {
    val logo = item.logo
    if (!logo.isNullOrEmpty()) {
        AsyncImage(
            model = logo,
            contentDescription = item.name,
            modifier = Modifier.heightIn(max = 120.dp).widthIn(max = 460.dp),
        )
    } else {
        Text(
            item.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.displayMedium,
        )
    }
}

/** Type, genres, year and rating, dot-separated like the Netflix billboard. */
@Composable
private fun HeroMetadata(item: MetaPreview) {
    val type = item.type.replaceFirstChar { it.uppercase() }
    val parts =
        buildList {
            if (type.isNotEmpty()) add(type)
            addAll(item.genres.take(2))
            yearOf(item.releaseInfo)?.let { add(it) }
            item.imdbRating?.let { add("★ ${round1(it)}") }
        }
    if (parts.isEmpty()) return
    Spacer(Modifier.height(AppSpacing.md))
    Row(verticalAlignment = Alignment.CenterVertically) {
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                Text(
                    "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm),
                )
            }
            Text(part, style = MaterialTheme.typography.labelLarge, color = AppColors.textPrimary)
        }
    }
}

/** Pill action button; the primary variant is the solid white call to action. */
@Composable
private fun HeroButton(
    onClick: () -> Unit,
    primary: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (primary) Color.White else Color.White.copy(alpha = AppAlpha.HERO_ACTION),
                contentColor = if (primary) Color.Black else Color.White,
            ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        modifier = Modifier.height(44.dp),
        content = content,
    )
}

private fun round1(value: Double): String {
    val scaled = kotlin.math.round(value * 10) / 10.0
    return scaled.toString()
}
