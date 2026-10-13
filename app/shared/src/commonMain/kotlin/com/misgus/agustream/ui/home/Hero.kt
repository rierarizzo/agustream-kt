package com.misgus.agustream.ui.home

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_more_info
import agustream.app.shared.generated.resources.action_play
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.ActionButton
import com.misgus.agustream.ui.components.ArtworkFilterQuality
import com.misgus.agustream.ui.components.LogoFilterQuality
import com.misgus.agustream.ui.components.RemoteImage
import com.misgus.agustream.ui.components.ratingText
import com.misgus.agustream.ui.theme.AppAlpha
import com.misgus.agustream.ui.theme.AppLogoSize
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.AppSynopsis
import com.misgus.agustream.ui.theme.AppType
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.screenPadding
import org.jetbrains.compose.resources.stringResource

/** Share of the window width the desktop artwork occupies. */
private const val HERO_IMAGE_WIDTH_FRACTION = 0.75f

/**
 * Height of the desktop frame, as a share of the window width: the frame is
 * sized as if the artwork spanned this much width, so the wider artwork above
 * is cropped at the bottom instead of growing the billboard taller.
 */
private const val HERO_DESKTOP_HEIGHT_FRACTION = 0.7f

/** Aspect ratio used until the artwork has decoded, so the frame has a height. */
private const val HERO_FALLBACK_ASPECT_RATIO = 1920f / 823f

/** Widest the copy column ever grows, so the synopsis keeps a comfortable measure. */
private val HERO_CONTENT_MAX_WIDTH = 840.dp

/** Gap between the bottom of the billboard and the copy actions. */
private val HERO_CONTENT_BOTTOM_MARGIN = 100.dp

/** Vertical rhythm of the copy column. */
private val HERO_LOGO_METADATA_SPACE = 20.dp
private val HERO_METADATA_SYNOPSIS_SPACE = 22.dp
private val HERO_SYNOPSIS_ACTIONS_SPACE = 28.dp

/** Gap between the rating star and its number. */
private val HERO_STAR_GAP = 4.dp

/** Carousel paginator: inset from the right edge, dot size, gap and animation. */
private val HERO_PAGINATOR_END_MARGIN = 30.dp
private val HERO_PAGINATOR_ACTIVE_WIDTH = 30.dp
private val HERO_PAGINATOR_DOT = 6.dp
private val HERO_PAGINATOR_GAP = 8.dp
private const val HERO_PAGINATOR_ANIMATION_MS = 250

/**
 * Height of the full-width billboard for [maxWidth], used on a phone where the
 * artwork spans the whole width. Sizing the frame to the artwork's own aspect
 * ratio lets `Fit` show the whole image, with no crop and no bars.
 */
private fun heroHeight(
    maxWidth: Dp,
    aspectRatio: Float,
): Dp = maxWidth / aspectRatio

/**
 * Height of the desktop billboard. The artwork spans [HERO_IMAGE_WIDTH_FRACTION]
 * of [maxWidth] and is pinned to the top-right of the frame; the frame is sized
 * from [HERO_DESKTOP_HEIGHT_FRACTION], a touch shorter than the artwork, so the
 * bottom is cropped instead of stretching the billboard taller.
 */
private fun desktopHeroHeight(
    maxWidth: Dp,
    aspectRatio: Float,
): Dp = (maxWidth * HERO_DESKTOP_HEIGHT_FRACTION) / aspectRatio

/**
 * Compact / Android billboard: in the scroll flow, full-bleed, complete artwork.
 *
 * A phone cannot show the wide desktop composition, so it keeps the simpler
 * full-width frame that scrolls with the content.
 */
@Composable
internal fun Hero(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
) {
    if (items.isEmpty()) return
    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { items.size })
    // Each page reports the aspect ratio of its decoded artwork; the billboard
    // adopts the current page's, so `Fit` never has to letterbox or crop.
    val aspects = remember(items) { mutableStateMapOf<Int, Float>() }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val aspect = aspects[pagerState.currentPage] ?: HERO_FALLBACK_ASPECT_RATIO
        val height = heroHeight(maxWidth, aspect)
        HeroCarousel(
            items = items,
            pagerState = pagerState,
            height = height,
            wide = false,
            backdropWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) },
            backdropHeightPx = with(density) { height.roundToPx().coerceAtLeast(1) },
            onBackdropAspect = { page, ratio -> aspects[page] = ratio },
            onOpen = onOpen,
            onPlay = onPlay,
        )
    }
}

/**
 * Desktop billboard: in the scroll flow, with the artwork scaled to
 * [HERO_IMAGE_WIDTH_FRACTION] of the window, pinned to the top-right and
 * cropped at the bottom so the billboard does not grow taller.
 *
 * A single aspect keeps the frame (and the sections below it) stable while the
 * pages swipe; the first decoded artwork sets it.
 */
@Composable
internal fun WideHero(
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
    maxHeight: Dp = Dp.Infinity,
) {
    if (items.isEmpty()) return
    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { items.size })
    var aspect by remember(items) { mutableStateOf(HERO_FALLBACK_ASPECT_RATIO) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // The cap keeps the hero from eating the whole viewport, so a row of the
        // content below stays on screen; the artwork crops a little more instead.
        val height = minOf(desktopHeroHeight(maxWidth, aspect), maxHeight)
        // Decode the full artwork at its display width, so `Crop` has the whole
        // bitmap to trim from and not just the shorter frame.
        val artworkHeight = (maxWidth * HERO_IMAGE_WIDTH_FRACTION) / aspect
        HeroCarousel(
            items = items,
            pagerState = pagerState,
            height = height,
            wide = true,
            backdropWidthPx = with(density) { (maxWidth * HERO_IMAGE_WIDTH_FRACTION).roundToPx().coerceAtLeast(1) },
            backdropHeightPx = with(density) { artworkHeight.roundToPx().coerceAtLeast(1) },
            onBackdropAspect = { page, ratio -> if (page == 0) aspect = ratio },
            onOpen = onOpen,
            onPlay = onPlay,
        )
    }
}

/** Shared pager, paginator and preloader; [wide] selects the artwork placement. */
@Composable
private fun HeroCarousel(
    items: List<MetaPreview>,
    pagerState: PagerState,
    height: Dp,
    wide: Boolean,
    backdropWidthPx: Int,
    backdropHeightPx: Int,
    onBackdropAspect: (Int, Float) -> Unit,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
) {
    val compact = LocalWindowSize.current == WindowSize.COMPACT
    val density = LocalDensity.current

    Box(Modifier.fillMaxWidth().height(height)) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { page -> items[page].id },
        ) { page ->
            HeroSlide(
                item = items[page],
                compact = compact,
                wide = wide,
                onBackdropAspect = { ratio -> onBackdropAspect(page, ratio) },
                onOpen = { onOpen(items[page]) },
                onPlay = { onPlay(items[page]) },
            )
        }
        if (items.size > 1) {
            HeroPaginator(
                pageCount = items.size,
                pagerState = pagerState,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = HERO_PAGINATOR_END_MARGIN,
                            // Sit on the actions' baseline, like the sample.
                            bottom = HERO_CONTENT_BOTTOM_MARGIN,
                        ),
            )
        }
    }
    HeroArtworkPreloader(
        items = items,
        compact = compact,
        backdropWidthPx = backdropWidthPx,
        backdropHeightPx = backdropHeightPx,
        logoHeightPx = with(density) { AppLogoSize.homeHeight.roundToPx().coerceAtLeast(1) },
        logoMaxWidthPx = with(density) { AppLogoSize.maxWidth.roundToPx().coerceAtLeast(1) },
    )
}

/** One billboard slide: complete artwork under the background washes and the copy. */
@Composable
private fun HeroSlide(
    item: MetaPreview,
    compact: Boolean,
    wide: Boolean,
    onBackdropAspect: (Float) -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    val backdrop = if (compact) item.poster ?: item.background else item.background ?: item.poster
    // The background shows through if the artwork never loads, so a slow or
    // missing image never leaves a bright gap.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!backdrop.isNullOrEmpty()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                // A phone frame tracks the artwork's aspect ratio, so `Fit` shows
                // it whole; the desktop frame is a touch shorter, so `Crop` trims
                // the bottom and keeps the top edge pinned.
                contentScale = if (wide) ContentScale.Crop else ContentScale.Fit,
                alignment = if (wide) Alignment.TopCenter else Alignment.Center,
                filterQuality = ArtworkFilterQuality,
                onSuccess = { state ->
                    val image = state.result.image
                    if (image.width > 0 && image.height > 0) {
                        onBackdropAspect(image.width.toFloat() / image.height.toFloat())
                    }
                },
                modifier =
                    if (wide) {
                        // Pinned to the top-right corner, at the billboard's width.
                        Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(HERO_IMAGE_WIDTH_FRACTION)
                            .fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    },
            )
        }
        // Washes over the artwork, in draw order. The last child paints on top,
        // and every one ends on the page background so the hero has no seam.
        Box(Modifier.fillMaxSize().background(if (wide) AppScrim.heroWideLeft else AppScrim.heroHorizontal))
        Box(Modifier.fillMaxSize().background(AppScrim.heroBottom))
        Box(Modifier.fillMaxSize().background(AppScrim.heroTop))
        Box(Modifier.fillMaxSize().background(AppScrim.heroRightVignette))
        HeroCopy(item, compact, onOpen, onPlay)
    }
}

/** The copy block: title logo, metadata, synopsis and the existing actions. */
@Composable
private fun BoxScope.HeroCopy(
    item: MetaPreview,
    compact: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = LocalWindowSize.current.screenPadding,
                    end = LocalWindowSize.current.screenPadding,
                    bottom = HERO_CONTENT_BOTTOM_MARGIN,
                ).widthIn(max = HERO_CONTENT_MAX_WIDTH),
    ) {
        HeroTitle(item.name, if (compact) null else item.logo)
        Spacer(Modifier.height(HERO_LOGO_METADATA_SPACE))
        HeroMetadata(item)
        item.description?.takeIf { it.isNotEmpty() }?.let { synopsis ->
            Spacer(Modifier.height(HERO_METADATA_SYNOPSIS_SPACE))
            Text(
                synopsis,
                maxLines = AppSynopsis.MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                style = AppType.heroBody(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                modifier = Modifier.widthIn(max = AppSynopsis.maxWidth),
            )
        }
        Spacer(Modifier.height(HERO_SYNOPSIS_ACTIONS_SPACE))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ActionButton(text = stringResource(Res.string.action_play), icon = Icons.Default.PlayArrow, onClick = onPlay)
            ActionButton(text = stringResource(Res.string.action_more_info), icon = Icons.Outlined.Info, onClick = onOpen, primary = false)
        }
    }
}

/** Title logo when the metadata provides one, otherwise the name as a headline. */
@Composable
private fun HeroTitle(
    name: String,
    logo: String?,
) {
    if (!logo.isNullOrEmpty()) {
        RemoteImage(
            url = logo,
            contentDescription = name,
            contentScale = ContentScale.Fit,
            filterQuality = LogoFilterQuality,
            fadeIn = true,
            modifier = Modifier.height(AppLogoSize.homeHeight).widthIn(max = AppLogoSize.maxWidth),
        )
    } else {
        Text(
            name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = AppType.heroTitle(),
        )
    }
}

/** `year • ★ rating • genres`, with the star in the theme's accent colour. */
@Composable
private fun HeroMetadata(item: MetaPreview) {
    val year = yearOf(item.releaseInfo)
    val rating = item.imdbRating
    val genres = item.genres
    if (year == null && rating == null && genres.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        var hasPart = false
        if (year != null) {
            HeroMetaText(year)
            hasPart = true
        }
        if (rating != null) {
            if (hasPart) HeroMetaSeparator()
            Text("★", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(HERO_STAR_GAP))
            HeroMetaText(ratingText(rating))
            hasPart = true
        }
        if (genres.isNotEmpty()) {
            if (hasPart) HeroMetaSeparator()
            HeroMetaText(genres.joinToString(", "))
        }
    }
}

@Composable
private fun HeroMetaText(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun HeroMetaSeparator() {
    Text(
        "•",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = AppSpacing.sm),
    )
}

/** Carousel paginator: a growing pill for the active page, dots for the rest. */
@Composable
private fun HeroPaginator(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(HERO_PAGINATOR_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 0 until pageCount) {
            val selected = index == pagerState.currentPage
            val width by animateDpAsState(
                targetValue = if (selected) HERO_PAGINATOR_ACTIVE_WIDTH else HERO_PAGINATOR_DOT,
                animationSpec = motionTween(HERO_PAGINATOR_ANIMATION_MS),
                label = "heroIndicatorWidth",
            )
            val color by animateColorAsState(
                targetValue =
                    if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = AppAlpha.HERO_INDICATOR)
                    },
                animationSpec = motionTween(HERO_PAGINATOR_ANIMATION_MS),
                label = "heroIndicatorColor",
            )
            Box(
                modifier =
                    Modifier
                        .width(width)
                        .height(HERO_PAGINATOR_DOT)
                        .clip(RoundedCornerShape(HERO_PAGINATOR_DOT / 2))
                        .background(color),
            )
        }
    }
}

/**
 * Warms Coil's cache with every hero artwork, so swiping the carousel paints
 * from memory instead of starting a network round-trip mid-gesture. The
 * requests are cancelled with the composition if the items change.
 */
@Composable
private fun HeroArtworkPreloader(
    items: List<MetaPreview>,
    compact: Boolean,
    backdropWidthPx: Int,
    backdropHeightPx: Int,
    logoHeightPx: Int,
    logoMaxWidthPx: Int,
) {
    val context = LocalPlatformContext.current
    LaunchedEffect(items, compact, backdropWidthPx, backdropHeightPx, logoHeightPx, logoMaxWidthPx) {
        if (items.isEmpty()) return@LaunchedEffect
        val loader = SingletonImageLoader.get(context)
        items.forEach { item ->
            val backdrop =
                if (compact) item.poster ?: item.background else item.background ?: item.poster
            backdrop?.let { url ->
                loader.enqueue(
                    ImageRequest
                        .Builder(context)
                        .data(url)
                        .size(backdropWidthPx, backdropHeightPx)
                        .build(),
                )
            }
            item.logo?.let { url ->
                loader.enqueue(
                    ImageRequest
                        .Builder(context)
                        .data(url)
                        .size(logoMaxWidthPx, logoHeightPx)
                        .build(),
                )
            }
        }
    }
}
