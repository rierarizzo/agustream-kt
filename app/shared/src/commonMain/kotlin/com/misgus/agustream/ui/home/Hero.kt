package com.misgus.agustream.ui.home

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_watch_now
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.ui.components.AppIconButton
import com.misgus.agustream.ui.components.BackdropFilterQuality
import com.misgus.agustream.ui.components.HeroActionRow
import com.misgus.agustream.ui.components.HeroActionRowInset
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
import com.misgus.agustream.ui.theme.LocalMotionEnabled
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.motionSpring
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.screenPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Share of the window width the desktop artwork occupies. */
private const val HERO_IMAGE_WIDTH_FRACTION = 0.75f

/** Aspect ratio used until the artwork has decoded, so the frame has a height. */
private const val HERO_FALLBACK_ASPECT_RATIO = 1920f / 823f

/**
 * Height of the compact billboard as a multiple of its width.
 *
 * A phone cannot show the wide desktop composition, so it crops the same
 * backdrop into a tall band: 1.3 keeps the centre of the artwork, where the
 * subject usually is, and gives the centred logo and actions room to breathe.
 */
private const val HERO_COMPACT_HEIGHT_RATIO = 1.3f

/** Widest the copy column ever grows, so the synopsis keeps a comfortable measure. */
private val HERO_CONTENT_MAX_WIDTH = 840.dp

/** Gap between the bottom of the billboard and the copy actions. */
private val HERO_CONTENT_BOTTOM_MARGIN = 100.dp

/**
 * Gap between the bottom of the compact billboard and the copy actions.
 *
 * Keeps the actions just above the paginator and deep in the bottom fade,
 * without letting the icon row crowd the dots (the wide billboard has far more
 * room below its row).
 */
private val HERO_COMPACT_CONTENT_BOTTOM_MARGIN = 64.dp

/**
 * Layout size of the billboard's play glyph.
 *
 * The Material play arrow only fills ~52% of its viewBox height, against the
 * ~78% of the heart beside it, so the layout box is enlarged to make the drawn
 * triangle the same optical height as the row instead of reading smaller.
 */
private val HERO_PLAY_ICON_SIZE = 36.dp

/**
 * Vertical rhythm of the copy column, in order: title, metadata, synopsis,
 * actions. The synopsis sits above the actions here, while the detail billboard
 * keeps actions first; the values are tighter, since the home copy sits over
 * cropped artwork.
 */
private val HERO_LOGO_METADATA_SPACE = 12.dp
private val HERO_METADATA_SYNOPSIS_SPACE = 18.dp

/**
 * Same rhythm as the metadata-to-synopsis gap, discounting the transparent
 * padding of the action row ([HeroActionRowInset]).
 */
private val HERO_SYNOPSIS_ACTIONS_SPACE = HERO_METADATA_SYNOPSIS_SPACE - HeroActionRowInset

/**
 * Tighter vertical rhythm on a phone, where the copy shares a shorter band.
 *
 * The actions follow the metadata directly (no synopsis), so their spacer
 * discounts [HeroActionRowInset] too; the metadata stays a comfortable step
 * below the title instead of the row floating away from it.
 */
private val HERO_COMPACT_LOGO_METADATA_SPACE = 8.dp
private val HERO_COMPACT_METADATA_ACTIONS_SPACE = (12.dp - HeroActionRowInset).coerceAtLeast(0.dp)

/**
 * Widest the hero copy (not the artwork) slides during a page change. Small on
 * purpose: just enough to read the swipe direction, not a full-screen slide.
 */
private val HERO_CONTENT_SLIDE_MAX = 64.dp

/**
 * Backdrop drift against a swipe, as a fraction of the page width, and the
 * overscan that keeps the drift from exposing an edge.
 *
 * The two slides do not dissolve perfectly on top of each other: a small drift
 * and zoom make the change read as layers instead of a flat double exposure.
 */
private const val HERO_BACKDROP_PARALLAX = 0.055f
private const val HERO_BACKDROP_SCALE = 1.14f

/**
 * How far a drag on the action row must travel, or how fast it must fling, to
 * commit to the next slide instead of snapping back.
 */
private val HERO_SWIPE_THRESHOLD = 48.dp
private const val HERO_SWIPE_VELOCITY_THRESHOLD = 300f

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
private val HERO_PAGINATOR_DOT_SIZE = 7.dp
private val HERO_PAGINATOR_GAP = 6.dp
private val HERO_PAGINATOR_BOTTOM_MARGIN = 24.dp

/** On a phone the paginator sits higher, clear of the gesture bar. */
private val HERO_COMPACT_PAGINATOR_BOTTOM_MARGIN = 32.dp
private const val HERO_PAGINATOR_ANIMATION_MS = 250

/**
 * First page of the infinite hero pager.
 *
 * A large multiple of [itemCount] near the middle of the range, so there is room
 * to swipe both ways and the auto-advance always moves forward: the last slide
 * no longer scrolls back through every page to reach the first. `page %
 * itemCount` maps a page back to its item.
 */
private fun heroStartingPage(itemCount: Int): Int = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2) % itemCount

/**
 * Compact / Android billboard: in the scroll flow, full-bleed and cropped.
 *
 * A phone cannot show the wide desktop composition, so it crops the same
 * backdrop into a tall band, centres the logo and the actions, washes it only
 * with a bottom fade, and drops the synopsis, which has no room to read on a
 * small screen.
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

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val height = maxWidth * HERO_COMPACT_HEIGHT_RATIO
        HeroCarousel(
            items = items,
            height = height,
            pageWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) },
            wide = false,
            savedIds = savedIds,
            busyIds = busyIds,
            backdropWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) },
            backdropHeightPx = with(density) { height.roundToPx().coerceAtLeast(1) },
            onBackdropAspect = { _, _ -> },
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
    var aspect by remember(items) { mutableStateOf(HERO_FALLBACK_ASPECT_RATIO) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val frameHeight = height
        // Decode the full artwork at its display width; if the frame ends up
        // taller than the artwork's aspect, decode to the frame height so `Crop`
        // does not upscale.
        val artworkHeight = maxOf((maxWidth * HERO_IMAGE_WIDTH_FRACTION) / aspect, frameHeight)
        HeroCarousel(
            items = items,
            height = frameHeight,
            pageWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) },
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
    height: Dp,
    pageWidthPx: Int,
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
    // Infinite pager so the auto-advance and a swipe wrap around instead of
    // scrolling back through every page from the last slide to the first. The
    // item shown is `page % items.size`; recreating on a size change keeps the
    // current page valid when the featured set rotates.
    val pagerState =
        key(items.size) {
            rememberPagerState(
                initialPage = if (items.size > 1) heroStartingPage(items.size) else 0,
                pageCount = { if (items.size > 1) Int.MAX_VALUE else 1 },
            )
        }
    val compact = LocalWindowSize.current == WindowSize.COMPACT
    val density = LocalDensity.current
    val motionEnabled = LocalMotionEnabled.current
    val snapSpec = motionSpring()
    val scope = rememberCoroutineScope()
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
    LaunchedEffect(pagerState, items.size, motionEnabled) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(HERO_AUTO_ADVANCE_INTERVAL)
            val sinceManual = lastManualInteraction?.elapsedNow()
            val coolingDown = sinceManual != null && sinceManual < HERO_MANUAL_COOLDOWN
            if (!pagerState.isScrollInProgress && !coolingDown) {
                autoScrolling.value = true
                try {
                    // A spring snap, like the rest of the app's larger surfaces,
                    // and a snap when motion is off.
                    pagerState.animateScrollToPage(
                        pagerState.currentPage + 1,
                        animationSpec = snapSpec,
                    )
                } finally {
                    autoScrolling.value = false
                }
            }
        }
    }

    // The page beside the settled one, if any. Reading the offset through
    // `derivedStateOf` keeps the layers from recomposing on every swipe frame:
    // the neighbour only changes when a page enters or leaves the viewport.
    val neighborPage by remember(pagerState) {
        derivedStateOf {
            when {
                pagerState.currentPageOffsetFraction > 0f -> pagerState.currentPage + 1
                pagerState.currentPageOffsetFraction < 0f -> pagerState.currentPage - 1
                else -> null
            }
        }
    }
    // Neighbour first and the settled page last, so the settled copy owns the hits.
    val layers = listOfNotNull(neighborPage, pagerState.currentPage)

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // The pager is only a gesture and offset driver: its pages are empty, so
        // the artwork and the copy are drawn as separate layers below. One alpha
        // over a whole page forces the billboard into an offscreen layer every
        // frame; per-primitive alpha does not.
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize()) {
            // Artwork first, then the washes once, then the copy: the wash is the
            // same for every slide, so it no longer draws per page.
            layers.forEach { page ->
                key(page) {
                    HeroBackdropLayer(
                        item = items[page % items.size],
                        wide = wide,
                        page = page,
                        pageWidthPx = pageWidthPx,
                        pagerState = pagerState,
                        onAspect = { ratio -> onBackdropAspect(page % items.size, ratio) },
                    )
                }
            }
            HeroWashes(wide)
            layers.forEach { page ->
                key(page) {
                    HeroCopy(
                        item = items[page % items.size],
                        compact = compact,
                        saved = items[page % items.size].id in savedIds,
                        savedBusy = items[page % items.size].id in busyIds,
                        onOpen = { onOpen(items[page % items.size]) },
                        onPlay = { onPlay(items[page % items.size]) },
                        onToggleSaved = { onToggleSaved(items[page % items.size]) },
                        pagerState = pagerState,
                        scope = scope,
                        onManualScroll = { lastManualInteraction = TimeSource.Monotonic.markNow() },
                        modifier =
                            Modifier.graphicsLayer {
                                // Deferred to the layer phase: the offset changes
                                // on every swipe frame, and an alpha over the
                                // copy alone stays off the whole-page layer.
                                alpha = heroLayerVisibility(pagerState, page)
                                translationX = -heroLayerOffset(pagerState, page) * HERO_CONTENT_SLIDE_MAX.toPx()
                            },
                    )
                }
            }
        }
        if (items.size > 1) {
            HeroPaginator(
                pageCount = items.size,
                pagerState = pagerState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (compact) HERO_COMPACT_PAGINATOR_BOTTOM_MARGIN else HERO_PAGINATOR_BOTTOM_MARGIN),
            )
        }
    }
    HeroArtworkPreloader(
        items = items,
        pagerState = pagerState,
        backdropWidthPx = backdropWidthPx,
        backdropHeightPx = backdropHeightPx,
        logoHeightPx =
            with(density) {
                AppLogoSize.opticalHeight(AppLogoSize.homeHeight).roundToPx().coerceAtLeast(1)
            },
        logoMaxWidthPx = with(density) { AppLogoSize.maxWidth.roundToPx().coerceAtLeast(1) },
    )
}

/**
 * Billboard artwork for one page, as its own layer.
 *
 * The alpha is read inside `graphicsLayer`, so a swipe updates the layer instead
 * of recomposing; a single-image layer is also what keeps the cross-fade off the
 * offscreen-buffer path a whole-page alpha would take.
 */
@Composable
private fun BoxScope.HeroBackdropLayer(
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
private fun HeroWashes(wide: Boolean) {
    if (wide) {
        Box(Modifier.fillMaxSize().background(AppScrim.heroWideLeft))
        Box(Modifier.fillMaxSize().background(AppScrim.heroBottom))
        Box(Modifier.fillMaxSize().background(AppScrim.heroTop))
        Box(Modifier.fillMaxSize().background(AppScrim.heroRightVignette))
    } else {
        Box(Modifier.fillMaxSize().background(AppScrim.heroBottomCompact))
    }
}

/** Signed distance of [page] from the settled page, in pages. */
private fun heroLayerOffset(
    pagerState: PagerState,
    page: Int,
): Float = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

/** Cross-fade alpha of [page] against the settled page. */
private fun heroLayerVisibility(
    pagerState: PagerState,
    page: Int,
): Float = (1f - heroLayerOffset(pagerState, page).absoluteValue).coerceIn(0f, 1f)

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

/**
 * Forwards a horizontal drag on the action row to the pager.
 *
 * The copy layers sit above the pager, so a drag that starts on one of their
 * buttons is caught by the button and never reaches the pager: siblings do not
 * share the hit path. This runs in the initial pass, drives the pager with the
 * raw delta and snaps on release, while a tap still falls through to the button.
 */
private fun Modifier.heroActionDrag(
    pagerState: PagerState,
    scope: CoroutineScope,
    onManualScroll: () -> Unit,
): Modifier =
    if (pagerState.pageCount <= 1) {
        this
    } else {
        pointerInput(pagerState) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val thresholdPx = HERO_SWIPE_THRESHOLD.toPx()
                val velocity = VelocityTracker().apply { addPosition(down.uptimeMillis, down.position) }
                val startPage = pagerState.currentPage
                var totalX = 0f
                var totalY = 0f
                var dragging = false
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    velocity.addPosition(change.uptimeMillis, change.position)
                    if (!change.pressed) {
                        if (dragging) {
                            onManualScroll()
                            val flung = velocity.calculateVelocity().x.absoluteValue > HERO_SWIPE_VELOCITY_THRESHOLD
                            val committed = totalX.absoluteValue > thresholdPx || flung
                            val target =
                                when {
                                    !committed -> startPage
                                    totalX > 0f -> startPage - 1
                                    totalX < 0f -> startPage + 1
                                    else -> startPage
                                }
                            scope.launch { pagerState.animateScrollToPage(target) }
                        }
                        break
                    }
                    val delta = change.position - change.previousPosition
                    totalX += delta.x
                    totalY += delta.y
                    if (!dragging) {
                        val horizontal = totalX.absoluteValue > viewConfiguration.touchSlop && totalX.absoluteValue > totalY.absoluteValue
                        val vertical = totalY.absoluteValue > viewConfiguration.touchSlop && totalY.absoluteValue > totalX.absoluteValue
                        when {
                            vertical -> break
                            horizontal -> {
                                dragging = true
                                onManualScroll()
                            }
                            else -> continue
                        }
                    }
                    pagerState.dispatchRawDelta(-delta.x)
                    change.consume()
                }
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
            val selected = index == pagerState.currentPage % pageCount
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
 * Warms Coil's cache with the next hero artwork, so the auto-advance and a
 * forward swipe paint from memory instead of starting a network round-trip
 * mid-gesture.
 *
 * Only the immediate neighbour is warmed, re-warmed as the page advances:
 * enqueuing the whole set at once spiked the decoder (six full-size backdrops
 * plus six logos) and competed with the visible page on entry. The request is
 * cancelled with the composition if the items or the sizes change.
 */
@Composable
private fun HeroArtworkPreloader(
    items: List<MetaPreview>,
    pagerState: PagerState,
    backdropWidthPx: Int,
    backdropHeightPx: Int,
    logoHeightPx: Int,
    logoMaxWidthPx: Int,
) {
    if (items.size <= 1) return
    val context = LocalPlatformContext.current
    // Reading `currentPage` here re-arms the preload only when the page settles,
    // not on every scroll frame.
    val nextPage = (pagerState.currentPage + 1) % items.size
    LaunchedEffect(items, nextPage, backdropWidthPx, backdropHeightPx, logoHeightPx, logoMaxWidthPx) {
        val loader = SingletonImageLoader.get(context)
        val item = items[nextPage]
        val backdrop = item.background ?: item.poster
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
