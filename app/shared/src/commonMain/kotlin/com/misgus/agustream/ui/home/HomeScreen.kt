package com.misgus.agustream.ui.home

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
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.PosterCaption
import com.misgus.agustream.ui.theme.WindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.motionTween
import com.misgus.agustream.ui.theme.railHeight
import com.misgus.agustream.ui.theme.safeTopInset
import com.misgus.agustream.ui.theme.screenPadding

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

    Box(Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            posterSize = settings.posterSize,
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
    onOpenDetail: (LibraryItem) -> Unit,
    onOpenPreview: (MetaPreview) -> Unit,
    onOpenCatalog: (com.misgus.agustream.core.repository.CatalogRef) -> Unit,
    onPlayPreview: (MetaPreview) -> Unit,
) {
    val windowSize = LocalWindowSize.current
    when {
        state.isLoading && !state.hasLoaded ->
            HomeSkeleton(posterSize = posterSize)
        state.error != null && state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load Home",
                message = state.error.toUserMessage(),
            )
        state.rows.isEmpty() && state.continueWatching.isEmpty() ->
            Message(
                icon = Icons.Outlined.Explore,
                title = "Nothing to show",
                message = "Install a metadata addon with catalogs to fill this Home.",
            )
        else ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) {
                if (state.featured.isNotEmpty()) {
                    Hero(items = state.featured, onOpen = onOpenPreview, onPlay = onPlayPreview)
                } else if (windowSize == WindowSize.COMPACT) {
                    // No billboard to tuck under the status bar: reserve its height.
                    Spacer(Modifier.height(safeTopInset))
                }
                if (state.continueWatching.isNotEmpty()) {
                    val compact = windowSize == WindowSize.COMPACT
                    SectionHeader(title = "Continue watching")
                    LazyRow(
                        modifier = Modifier.height(if (compact) CompactContinueHeight else ContinueHeight),
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
                    SectionHeader(title = row.title, onSeeAll = { onOpenCatalog(row.ref) })
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
                Spacer(Modifier.height(AppSpacing.xl))
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
        modifier = Modifier.width(width).clickableSurface(interaction, onTap),
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

/** Extracts the four-digit year from a Stremio `releaseInfo` string. */
internal fun yearOf(releaseInfo: String?): String? = Regex("(?:1[89]\\d\\d|20\\d\\d)").find(releaseInfo ?: "")?.value
