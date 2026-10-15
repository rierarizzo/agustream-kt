package com.misgus.pengu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.action_watch_now
import com.misgus.pengu.core.model.MetaPreview
import com.misgus.pengu.ui.components.AppIconButton
import com.misgus.pengu.ui.components.BackdropFilterQuality
import com.misgus.pengu.ui.components.HeroActionRow
import com.misgus.pengu.ui.components.HeroActionState
import com.misgus.pengu.ui.components.HeroSynopsis
import com.misgus.pengu.ui.components.MediaTitle
import com.misgus.pengu.ui.components.MetadataRow
import com.misgus.pengu.ui.components.metadataParts
import com.misgus.pengu.ui.icons.AppIcons
import com.misgus.pengu.ui.theme.AppLogoSize
import com.misgus.pengu.ui.theme.AppScrim
import com.misgus.pengu.ui.theme.AppSpacing
import com.misgus.pengu.ui.theme.LocalWindowSize
import com.misgus.pengu.ui.theme.screenPadding
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

/*
 * The per-page layers of the hero billboard: the artwork, the washes over it and
 * the copy block. [HeroCarousel] stacks them by page, so they read the pager
 * state but never own it.
 */

/**
 * Billboard artwork for one page, as its own layer.
 *
 * The alpha is read inside `graphicsLayer`, so a swipe updates the layer instead
 * of recomposing; a single-image layer is also what keeps the cross-fade off the
 * offscreen-buffer path a whole-page alpha would take.
 */
@Composable
internal fun BoxScope.HeroBackdropLayer(
    item: MetaPreview,
    wide: Boolean,
    page: Int,
    pageWidthPx: Int,
    pagerState: PagerState,
    onAspect: (Float) -> Unit,
) {
    val backdrop = item.background ?: item.poster
    if (backdrop.isNullOrEmpty()) return
    AsyncImage(
        model = backdrop,
        contentDescription = null,
        // Both frames are shorter than the wide artwork, so `Crop` trims it: the
        // desktop keeps the top edge pinned, the phone keeps the centre, where
        // the subject usually is.
        contentScale = ContentScale.Crop,
        alignment = if (wide) Alignment.TopCenter else Alignment.Center,
        filterQuality = BackdropFilterQuality,
        onSuccess = { state ->
            val image = state.result.image
            if (image.width > 0 && image.height > 0) {
                onAspect(image.width.toFloat() / image.height.toFloat())
            }
        },
        modifier =
            Modifier
                .then(
                    if (wide) {
                        // Pinned to the top-right corner, at the billboard's width.
                        Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(HERO_IMAGE_WIDTH_FRACTION)
                            .fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    },
                ).graphicsLayer {
                    alpha = heroLayerVisibility(pagerState, page)
                    // A small drift and overscan, so the two slides read as layers
                    // instead of a flat double exposure.
                    translationX = -heroLayerOffset(pagerState, page) * pageWidthPx * HERO_BACKDROP_PARALLAX
                    scaleX = HERO_BACKDROP_SCALE
                    scaleY = HERO_BACKDROP_SCALE
                },
    )
}

/**
 * The washes over the artwork, drawn once for the whole billboard.
 *
 * The last child paints on top, and every gradient ends on the page background
 * so the hero has no seam. Hoisting them out of the slides means the cross-fade
 * no longer draws a second full-screen gradient while it runs. The wide
 * billboard needs its edge scrims in different directions; a phone folds its
 * flat dim and bottom fade into one gradient.
 */
@Composable
internal fun HeroWashes(wide: Boolean) {
    if (wide) {
        Box(Modifier.fillMaxSize().background(AppScrim.heroWideLeft))
        Box(Modifier.fillMaxSize().background(AppScrim.heroBottom))
        Box(Modifier.fillMaxSize().background(AppScrim.heroTop))
        Box(Modifier.fillMaxSize().background(AppScrim.heroRightVignette))
    } else {
        Box(Modifier.fillMaxSize().background(AppScrim.heroBottomCompact))
    }
}

/** The copy block: title logo, metadata, synopsis and the existing actions. */
@Composable
internal fun BoxScope.HeroCopy(
    item: MetaPreview,
    compact: Boolean,
    saved: Boolean,
    savedBusy: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onToggleSaved: () -> Unit,
    pagerState: PagerState,
    scope: CoroutineScope,
    onManualScroll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = LocalWindowSize.current.screenPadding
    Column(
        modifier =
            modifier
                .align(if (compact) Alignment.BottomCenter else Alignment.BottomStart)
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = if (compact) HERO_COMPACT_CONTENT_BOTTOM_MARGIN else HERO_CONTENT_BOTTOM_MARGIN,
                ).widthIn(max = HERO_CONTENT_MAX_WIDTH),
        horizontalAlignment = if (compact) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        MediaTitle(
            name = item.name,
            logo = item.logo,
            logoHeight = AppLogoSize.homeHeight,
            textAlign = if (compact) TextAlign.Center else TextAlign.Start,
        )
        Spacer(Modifier.height(if (compact) HERO_COMPACT_LOGO_METADATA_SPACE else HERO_LOGO_METADATA_SPACE))
        MetadataRow(metadataParts(item.releaseInfo, genres = item.genres, rating = item.imdbRating, contentType = item.type))
        if (compact) {
            // No synopsis on a phone: there is no room to read it under the
            // centred logo, so the actions follow the metadata directly.
            Spacer(Modifier.height(HERO_COMPACT_METADATA_ACTIONS_SPACE))
        } else {
            Spacer(Modifier.height(HERO_METADATA_SYNOPSIS_SPACE))
            item.description?.takeIf { it.isNotEmpty() }?.let { synopsis ->
                HeroSynopsis(text = synopsis)
            }
            Spacer(Modifier.height(HERO_SYNOPSIS_ACTIONS_SPACE))
        }
        Row(
            modifier = Modifier.heroActionDrag(pagerState, scope, onManualScroll),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppIconButton(
                icon = AppIcons.Filled.PlayArrow,
                contentDescription = stringResource(Res.string.action_watch_now),
                onClick = onPlay,
                tint = MaterialTheme.colorScheme.primary,
                iconSize = HERO_PLAY_ICON_SIZE,
            )
            HeroActionRow(
                actions = HeroActionState(saved = saved, savedBusy = savedBusy),
                onToggleSaved = onToggleSaved,
                onInfo = onOpen,
            )
        }
    }
}
