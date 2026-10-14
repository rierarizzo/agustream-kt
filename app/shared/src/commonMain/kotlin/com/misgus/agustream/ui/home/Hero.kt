package com.misgus.agustream.ui.home

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_watch_now
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.ActionButton
import com.misgus.agustream.ui.components.ActionButtonStyle
import com.misgus.agustream.ui.components.ArtworkFilterQuality
import com.misgus.agustream.ui.components.HeroActionRow
import com.misgus.agustream.ui.components.HeroActionState
import com.misgus.agustream.ui.components.HeroSynopsis
import com.misgus.agustream.ui.components.MediaTitle
import com.misgus.agustream.ui.components.MetadataRow
import com.misgus.agustream.ui.components.metadataParts
import com.misgus.agustream.ui.icons.AppIcons
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppLogoSize
import com.misgus.agustream.ui.theme.AppScrim
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.screenPadding
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Share of the window width the desktop artwork occupies. */
private const val HERO_IMAGE_WIDTH_FRACTION = 0.75f

/** Aspect ratio used until the artwork has decoded, so the frame has a height. */
private const val HERO_FALLBACK_ASPECT_RATIO = 1920f / 823f

/** Widest the copy column ever grows, so the synopsis keeps a comfortable measure. */
private val HERO_CONTENT_MAX_WIDTH = 840.dp

/** Gap between the bottom of the billboard and the copy actions. */
private val HERO_CONTENT_BOTTOM_MARGIN = 100.dp

/**
 * Vertical rhythm of the copy column, in order: title, metadata, synopsis,
 * actions. The synopsis sits above the actions here, while the detail billboard
 * keeps actions first; the values are tighter, since the home copy sits over
 * cropped artwork.
 */
private val HERO_LOGO_METADATA_SPACE = 12.dp
private val HERO_METADATA_SYNOPSIS_SPACE = 18.dp
private val HERO_SYNOPSIS_ACTIONS_SPACE = 18.dp

/**
 * Widest the hero copy (not the artwork) slides during a page change. Small on
 * purpose: just enough to read the swipe direction, not a full-screen slide.
 */
private val HERO_CONTENT_SLIDE_MAX = 64.dp

/**
 * How long a slide stays before the carousel advances to the next one.
 *
 * In the common 5-8 s range for auto-rotating carousels (Swiper defaults to 3 s,
 * Bootstrap to 5 s): long enough to read the title, short enough to keep moving.
 */
private val HERO_AUTO_ADVANCE_INTERVAL = 7.seconds

/**
 * How long auto-advance stays paused after the user swipes the carousel.
 *
 * A manual change means the user picked that slide, so the rotation yields to
 * them instead of moving the billboard under their tap. A minute is long enough
 * to read the copy and use the actions without it shifting, and short enough
 * that the hero starts moving again on its own once they go idle; it is the
 * common "resume after interaction" window for auto-rotating carousels.
 */
private val HERO_MANUAL_COOLDOWN = 60.seconds

/** Carousel paginator: dot size, gap, bottom inset and colour animation. */
private val HERO_PAGINATOR_DOT_SIZE = 10.dp
private val HERO_PAGINATOR_GAP = 8.dp
private val HERO_PAGINATOR_BOTTOM_MARGIN = 24.dp
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
 * Compact / Android billboard: in the scroll flow, full-bleed, complete artwork.
 *
 * A phone cannot show the wide desktop composition, so it keeps the simpler
 * full-width frame that scrolls with the content.
 */
@Composable
internal fun Hero(
    items: List<MetaPreview>,
    savedIds: Set<String>,
    busyIds: Set<String>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
    onToggleSaved: (MetaPreview) -> Unit,
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
            savedIds = savedIds,
            busyIds = busyIds,
            backdropWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) },
            backdropHeightPx = with(density) { height.roundToPx().coerceAtLeast(1) },
            onBackdropAspect = { page, ratio -> aspects[page] = ratio },
            onOpen = onOpen,
            onPlay = onPlay,
            onToggleSaved = onToggleSaved,
        )
    }
}

/**
 * Desktop billboard: in the scroll flow, with the artwork scaled to
 * [HERO_IMAGE_WIDTH_FRACTION] of the window and pinned to the top-right.
 *
 * [height] is a fixed billboard height, so a horizontal resize changes how much
 * artwork is shown, not how tall the billboard is. A single aspect keeps the
 * frame (and the sections below it) stable while the pages swipe; the first
 * decoded artwork sets it.
 */
@Composable
internal fun WideHero(
    items: List<MetaPreview>,
    savedIds: Set<String>,
    busyIds: Set<String>,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
    onToggleSaved: (MetaPreview) -> Unit,
    height: Dp,
) {
    if (items.isEmpty()) return
    val density = LocalDensity.current
    val pagerState = rememberPagerState(pageCount = { items.size })
    var aspect by remember(items) { mutableStateOf(HERO_FALLBACK_ASPECT_RATIO) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val frameHeight = height
        // Decode the full artwork at its display width; if the frame ends up
        // taller than the artwork's aspect, decode to the frame height so `Crop`
        // does not upscale.
        val artworkHeight = maxOf((maxWidth * HERO_IMAGE_WIDTH_FRACTION) / aspect, frameHeight)
        HeroCarousel(
            items = items,
            pagerState = pagerState,
            height = frameHeight,
            wide = true,
            savedIds = savedIds,
            busyIds = busyIds,
            backdropWidthPx = with(density) { (maxWidth * HERO_IMAGE_WIDTH_FRACTION).roundToPx().coerceAtLeast(1) },
            backdropHeightPx = with(density) { artworkHeight.roundToPx().coerceAtLeast(1) },
            onBackdropAspect = { page, ratio -> if (page == 0) aspect = ratio },
            onOpen = onOpen,
            onPlay = onPlay,
            onToggleSaved = onToggleSaved,
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
    savedIds: Set<String>,
    busyIds: Set<String>,
    backdropWidthPx: Int,
    backdropHeightPx: Int,
    onBackdropAspect: (Int, Float) -> Unit,
    onOpen: (MetaPreview) -> Unit,
    onPlay: (MetaPreview) -> Unit,
    onToggleSaved: (MetaPreview) -> Unit,
) {
    val compact = LocalWindowSize.current == WindowSize.COMPACT
    val density = LocalDensity.current
    // Monotonic mark of the last manual interaction; auto-advance holds off until
    // a full cooldown has passed, then resumes once the user goes idle.
    var lastManualInteraction by remember(pagerState) { mutableStateOf<TimeMark?>(null) }
    // Set while our own animation drives the pager, so the watcher below can tell
    // an auto-advance from something the user did.
    val autoScrolling = remember { mutableStateOf(false) }
    // Any scroll we did not start counts as manual input. Watching
    // `isScrollInProgress` covers both a drag on a phone and a wheel / trackpad
    // swipe on desktop; `interactionSource` alone would miss the wheel, since it
    // only dispatches drag events.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect { inProgress ->
            if (inProgress && !autoScrolling.value) {
                lastManualInteraction = TimeSource.Monotonic.markNow()
            }
        }
    }
    // Auto-advance. The effect is keyed on stable inputs on purpose: keying it on
    // `isScrollInProgress` would cancel it (and the animation) the moment the
    // animation sets that flag itself, so the page would never move. A tick that
    // lands while the user is dragging, or while the post-swipe cooldown is still
    // running, is skipped instead.
    LaunchedEffect(pagerState, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(HERO_AUTO_ADVANCE_INTERVAL)
            val sinceManual = lastManualInteraction?.elapsedNow()
            val coolingDown = sinceManual != null && sinceManual < HERO_MANUAL_COOLDOWN
            if (!pagerState.isScrollInProgress && !coolingDown) {
                autoScrolling.value = true
                try {
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
                } finally {
                    autoScrolling.value = false
                }
            }
        }
    }

    Box(Modifier.fillMaxWidth().height(height).background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { page -> items[page].id },
        ) { page ->
            // Cross-fade instead of slide: the pager's horizontal offset is
            // cancelled and the page's alpha follows the same offset, so one
            // billboard dissolves through the page background into the next
            // while the swipe still scrubs the transition.
            val signedOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val distance = signedOffset.absoluteValue
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        // The focused page draws on top, so its buttons own the
                        // hits: the faded neighbours sit at the same place and
                        // would otherwise capture the click (alpha does not
                        // affect hit-testing).
                        .zIndex(-distance)
                        .graphicsLayer {
                            translationX = signedOffset * size.width
                            alpha = (1f - distance).coerceIn(0f, 1f)
                        },
            ) {
                HeroSlide(
                    item = items[page],
                    compact = compact,
                    wide = wide,
                    saved = items[page].id in savedIds,
                    savedBusy = items[page].id in busyIds,
                    signedOffset = signedOffset,
                    onBackdropAspect = { ratio -> onBackdropAspect(page, ratio) },
                    onOpen = { onOpen(items[page]) },
                    onPlay = { onPlay(items[page]) },
                    onToggleSaved = { onToggleSaved(items[page]) },
                )
            }
        }
        if (items.size > 1) {
            HeroPaginator(
                pageCount = items.size,
                pagerState = pagerState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = HERO_PAGINATOR_BOTTOM_MARGIN),
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
    saved: Boolean,
    savedBusy: Boolean,
    signedOffset: Float,
    onBackdropAspect: (Float) -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val backdrop = if (compact) item.poster ?: item.background else item.background ?: item.poster
    // The artwork cross-fades, but the copy slides a little against the swipe,
    // so the user can read whether the change is going forward or back.
    val slidePx = with(LocalDensity.current) { HERO_CONTENT_SLIDE_MAX.toPx() }
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
        HeroCopy(
            item = item,
            compact = compact,
            saved = saved,
            savedBusy = savedBusy,
            onOpen = onOpen,
            onPlay = onPlay,
            onToggleSaved = onToggleSaved,
            modifier = Modifier.graphicsLayer { translationX = -signedOffset * slidePx },
        )
    }
}

/** The copy block: title logo, metadata, synopsis and the existing actions. */
@Composable
private fun BoxScope.HeroCopy(
    item: MetaPreview,
    compact: Boolean,
    saved: Boolean,
    savedBusy: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = LocalWindowSize.current.screenPadding,
                    end = LocalWindowSize.current.screenPadding,
                    bottom = HERO_CONTENT_BOTTOM_MARGIN,
                ).widthIn(max = HERO_CONTENT_MAX_WIDTH),
    ) {
        MediaTitle(
            name = item.name,
            logo = if (compact) null else item.logo,
            logoHeight = AppLogoSize.homeHeight,
        )
        Spacer(Modifier.height(HERO_LOGO_METADATA_SPACE))
        MetadataRow(metadataParts(item.releaseInfo, genres = item.genres, rating = item.imdbRating))
        Spacer(Modifier.height(HERO_METADATA_SYNOPSIS_SPACE))
        item.description?.takeIf { it.isNotEmpty() }?.let { synopsis ->
            HeroSynopsis(text = synopsis)
            Spacer(Modifier.height(HERO_SYNOPSIS_ACTIONS_SPACE))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ActionButton(
                text = stringResource(Res.string.action_watch_now),
                icon = AppIcons.Filled.PlayArrow,
                onClick = onPlay,
                style = ActionButtonStyle.Accent,
            )
            HeroActionRow(
                actions = HeroActionState(saved = saved, savedBusy = savedBusy),
                onToggleSaved = onToggleSaved,
                onInfo = onOpen,
            )
        }
    }
}

/** Carousel paginator: an equal dot per page, grey when idle and white when active. */
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
            val color by animateColorAsState(
                targetValue =
                    if (selected) {
                        AppColors.heroIndicatorActive
                    } else {
                        AppColors.heroIndicatorInactive
                    },
                animationSpec = motionTween(HERO_PAGINATOR_ANIMATION_MS),
                label = "heroIndicatorColor",
            )
            Box(
                modifier =
                    Modifier
                        .size(HERO_PAGINATOR_DOT_SIZE)
                        .clip(CircleShape)
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
