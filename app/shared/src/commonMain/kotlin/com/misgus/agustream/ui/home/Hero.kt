package com.misgus.agustream.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.MediaTitle
import com.misgus.agustream.ui.components.MetadataRow
import com.misgus.agustream.ui.components.PillButton
import com.misgus.agustream.ui.components.RemoteImage
import com.misgus.agustream.ui.components.ratingText
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowHeight
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.safeTopInset
import com.misgus.agustream.ui.theme.screenPadding

/** Billboard crop ratio (Netflix-style wide banner). */
internal const val HERO_ASPECT_RATIO = 1920f / 823f

/** Portrait crop used on a compact window, where the poster carries the hero. */
internal const val COMPACT_HERO_ASPECT_RATIO = 2f / 3f

/** Blur applied to the cover-filled copy behind the complete artwork. */
private val HERO_FILL_BLUR = 24.dp

/** How much the blurred copy is dimmed, so it does not compete with the artwork. */
private const val HERO_FILL_DIM = 0.4f

/** Featured billboard carousel. */
@Composable
internal fun Hero(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    // A phone cannot show the wide billboard, so it gets a taller crop of the
    // poster instead, with the name as text rather than the title logo.
    val compact = LocalWindowSize.current == WindowSize.COMPACT
    val windowHeight = LocalWindowHeight.current

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Full-bleed: the billboard reaches the window edges and tucks under the
        // floating navigation, so it reads as an immersive backdrop instead of an
        // inset card. Its height follows the artwork's aspect ratio (never a
        // forced crop), and is only capped so it cannot grow past the viewport on
        // a very wide, short window.
        val ratio = if (compact) COMPACT_HERO_ASPECT_RATIO else HERO_ASPECT_RATIO
        val naturalHeight = maxWidth / ratio
        val heroHeight = if (windowHeight.value.isFinite()) minOf(naturalHeight, windowHeight) else naturalHeight
        Box(Modifier.fillMaxWidth().height(heroHeight)) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
            ) { page ->
                HeroSlide(
                    item = items[page],
                    compact = compact,
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
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.HERO_INDICATOR)
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
    compact: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    val backdrop = if (compact) item.poster ?: item.background else item.background ?: item.poster
    // The gradient shows through if the artwork never loads, so a slow or
    // missing image never leaves a bright gap.
    Box(Modifier.fillMaxSize().background(AppScrim.detailBackdrop)) {
        if (!backdrop.isNullOrEmpty()) {
            // A blurred, cover-filled copy keeps the frame full-bleed when the
            // artwork does not match the box, so no side bars are left...
            RemoteImage(
                url = backdrop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(HERO_FILL_BLUR),
            )
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = HERO_FILL_DIM)))
            // ...and the complete artwork sits on top, never cropped.
            RemoteImage(
                url = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Netflix-style scrim: dark on the left column and along the bottom, so
        // the copy stays readable no matter how bright the artwork is.
        Box(Modifier.fillMaxSize().background(AppScrim.mediaHorizontal))
        Box(Modifier.fillMaxSize().background(AppScrim.mediaBottom))
        if (compact) {
            // The billboard runs under the status bar, so a short top fade keeps
            // the system icons legible over bright artwork.
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(safeTopInset + AppSpacing.xl)
                    .background(AppScrim.detailTop),
            )
        }
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(LocalWindowSize.current.screenPadding)
                    .widthIn(max = 620.dp),
        ) {
            MediaTitle(item.name, if (compact) null else item.logo)
            MetadataRow(heroMetadataParts(item))
            item.description?.takeIf { it.isNotEmpty() }?.let {
                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    it,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = AppType.heroBody(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
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
