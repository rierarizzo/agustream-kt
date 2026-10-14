package com.misgus.agustream.ui.home

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.empty_nothing_to_show
import agustream.app.shared.generated.resources.home_continue_watching
import agustream.app.shared.generated.resources.home_empty_message
import agustream.app.shared.generated.resources.home_error_title
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.PosterImage
import com.misgus.agustream.ui.components.SectionHeader
import com.misgus.agustream.ui.components.SyncingIndicator
import com.misgus.agustream.ui.components.clickableSurface
import com.misgus.agustream.ui.components.contentTypeLabel
import com.misgus.agustream.ui.components.motionBorder
import com.misgus.agustream.ui.components.rememberInteraction
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppColors
import com.misgus.agustream.ui.theme.AppCorner
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowHeight
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.railHeight
import com.misgus.agustream.ui.theme.safeTopInset
import com.misgus.agustream.ui.theme.screenPadding
import org.jetbrains.compose.resources.stringResource

/** Home section: a featured carousel, "Continue watching" and catalog rows. */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigator: ContentNavigator,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsRepository = LocalAppServices.current.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()
    val windowSize = LocalWindowSize.current
    // The hero only fits one full row when the window is maximized; a floating
    // window can afford to clip it.
    val window = LocalAppServices.current.window
    val isMaximized by window.isMaximized.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            posterSize = settings.posterSize,
            isMaximized = isMaximized,
            onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
            onOpenPreview = { navigator.push(ContentRoute.Detail(LibraryItem.fromPreview(it))) },
            onOpenCatalog = { navigator.push(ContentRoute.Catalog(it)) },
            onPlayPreview = { preview ->
                onOpenStreams(
                    StreamsRequest(
                        type = preview.type,
                        id = preview.id,
                        title = preview.name,
                        year = yearOf(preview.releaseInfo),
                        background = preview.background,
                    ),
                )
            },
        )
        if (state.isSyncingWatched) {
            // A wide window already clears the system bars for the section; a
            // phone keeps the billboard full-bleed, so the indicator clears the
            // status bar itself.
            val topInset = if (windowSize == WindowSize.COMPACT) safeTopInset else 0.dp
            Box(Modifier.align(Alignment.TopEnd).padding(top = topInset + AppSpacing.md, end = AppSpacing.xl)) {
                SyncingIndicator()
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    posterSize: PosterSize,
    isMaximized: Boolean,
    onOpenDetail: (LibraryItem) -> Unit,
    onOpenPreview: (MetaPreview) -> Unit,
    onOpenCatalog: (com.misgus.agustream.core.repository.CatalogRef) -> Unit,
    onPlayPreview: (MetaPreview) -> Unit,
) {
    val windowSize = LocalWindowSize.current
    when {
        state.isLoading && !state.hasLoaded ->
            // No placeholder skeleton; the sections paint when the data arrives.
            Box(Modifier.fillMaxSize())
        state.error != null && state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = stringResource(Res.string.home_error_title),
                message = stringResource(state.error.toUserMessage()),
            )
        state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.Explore,
                title = stringResource(Res.string.empty_nothing_to_show),
                message = stringResource(Res.string.home_empty_message),
            )
        else -> {
            val compact = windowSize == WindowSize.COMPACT
            // Size the hero so exactly one full row of content lands at the bottom
            // of the viewport: a taller row shrinks it, a shorter one grows it (so
            // the second row never peeks). Only for a plain catalog row on a wide,
            // maximized window; Continue watching and the phone keep the natural
            // height and let the rows flow. The sections start right below the
            // hero, with no overlap, so a row can also be cut by the viewport edge.
            val viewportHeight = LocalWindowHeight.current
            val heroHeight =
                if (compact || !isMaximized || state.continueWatching.isNotEmpty()) {
                    null
                } else {
                    val firstRowHeight = posterSize.railHeight(windowSize)
                    (viewportHeight - HOME_SECTION_HEADER_HEIGHT - firstRowHeight)
                        .coerceAtLeast(HOME_HERO_MIN_HEIGHT)
                }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) {
                when {
                    state.featured.isNotEmpty() && !compact ->
                        WideHero(
                            items = state.featured,
                            onOpen = onOpenPreview,
                            onPlay = onPlayPreview,
                            height = heroHeight,
                        )
                    state.featured.isNotEmpty() ->
                        Hero(items = state.featured, onOpen = onOpenPreview, onPlay = onPlayPreview)
                    compact ->
                        // No billboard to tuck under the status bar: reserve its height.
                        Spacer(Modifier.height(safeTopInset))
                }
                Column {
                    if (state.continueWatching.isNotEmpty()) {
                        SectionHeader(title = stringResource(Res.string.home_continue_watching))
                        LazyRow(
                            modifier = Modifier.heightIn(min = if (compact) CompactContinueHeight else ContinueHeight),
                            contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
                        ) {
                            items(state.continueWatching) { entry ->
                                val cardWidth = if (compact) CompactContinueWidth else ContinueWidth
                                ContinueCard(entry, cardWidth) { onOpenDetail(entry.item) }
                            }
                        }
                    }
                    state.rows.forEach { row ->
                        SectionHeader(
                            title = row.title,
                            subtitle = contentTypeLabel(row.ref.type),
                            onSeeAll = { onOpenCatalog(row.ref) },
                        )
                        LazyRow(
                            modifier = Modifier.height(posterSize.railHeight(windowSize)),
                            contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
                        ) {
                            items(row.items) { item ->
                                MetaPosterCard(
                                    preview = item,
                                    width = posterSize.cellWidth(windowSize),
                                    watched = item.id in state.watchedIds,
                                    onTap = { onOpenPreview(item) },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.xl))
            }
        }
    }
}

@Composable
private fun ContinueCard(
    entry: ContinueEntry,
    width: Dp,
    onTap: () -> Unit,
) {
    val item = entry.item
    val progress = entry.progress
    val episode =
        if (progress.season == null || progress.episode == null) {
            null
        } else {
            "S${progress.season}E${progress.episode}"
        }
    val backdrop = item.background ?: item.poster

    val interaction = rememberInteraction()
    val borderColor =
        animateColorAsState(
            targetValue = if (interaction.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            animationSpec = motionTween(),
            label = "borderColor",
        )

    Column(
        modifier = Modifier.width(width).clickableSurface(interaction, onTap, indication = null),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .motionBorder(color = { borderColor.value }, width = 1.dp, cornerRadius = AppCorner.small)
                    .clip(MaterialTheme.shapes.small),
        ) {
            PosterImage(backdrop, Modifier.fillMaxSize())
            progress.fraction?.let { fraction ->
                LinearProgressIndicator(
                    progress = { fraction.toFloat() },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = AppColors.mediaScrim,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            item.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PosterCaption.title(),
        )
        episode?.let { Text(it, style = PosterCaption.meta()) }
    }
}

/** Width and rail height of a 16:9 "Continue watching" card, smaller on a phone. */
private val ContinueWidth = 336.dp
private val CompactContinueWidth = 224.dp
private val ContinueHeight = 248.dp
private val CompactContinueHeight = 184.dp

/**
 * Height of a section header plus a little breathing room, for the hero fit math:
 * the title line and the row paddings (`AppSpacing.lg` + `AppSpacing.md`), plus a
 * small gap so the poster year is not flush against the bottom edge. The header
 * height is the title's, so the (optional) See all button does not change it.
 */
private val HOME_SECTION_HEADER_HEIGHT = 76.dp

/** Floor for the hero height, so a short window cannot shrink it under its copy. */
private val HOME_HERO_MIN_HEIGHT = 480.dp

/** Extracts the four-digit year from a Stremio `releaseInfo` string. */
internal fun yearOf(releaseInfo: String?): String? = Regex("(?:1[89]\\d\\d|20\\d\\d)").find(releaseInfo ?: "")?.value
