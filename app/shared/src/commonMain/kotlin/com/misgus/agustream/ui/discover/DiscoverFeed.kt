package com.misgus.agustream.ui.discover

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.discover_empty_message
import agustream.app.shared.generated.resources.discover_empty_title
import agustream.app.shared.generated.resources.discover_error_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.SkeletonPosterGrid
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.icons.AppIcons
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.POSTER_CARD_ASPECT
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.screenPadding
import org.jetbrains.compose.resources.stringResource

/**
 * The discover feed: every catalog item in one infinite grid.
 *
 * It is embedded in the Search section, so it owns no loading trigger and no
 * filter control; the caller loads and pages it, and passes the already-filtered
 * [items]. A single mixed grid (like the Library) instead of per-catalog rails:
 * the catalog boundaries are an addon detail the user should not have to care
 * about. Reaching the end loads the next page.
 */
@Composable
fun DiscoverFeed(
    state: DiscoverUiState,
    items: List<MetaPreview>,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && !state.hasLoaded -> SkeletonPosterGrid(posterSize, modifier)
        state.error != null && state.items.isEmpty() ->
            Box(modifier, contentAlignment = Alignment.Center) {
                Message(
                    icon = AppIcons.Outlined.ErrorOutline,
                    title = stringResource(Res.string.discover_error_title),
                    message = stringResource(state.error.toUserMessage()),
                )
            }
        state.items.isEmpty() ->
            Box(modifier, contentAlignment = Alignment.Center) {
                Message(
                    icon = AppIcons.Outlined.Explore,
                    title = stringResource(Res.string.discover_empty_title),
                    message = stringResource(Res.string.discover_empty_message),
                )
            }
        else ->
            DiscoverGrid(
                items = items,
                isLoadingMore = state.isLoadingMore,
                posterSize = posterSize,
                onOpenDetail = onOpenDetail,
                onLoadMore = onLoadMore,
                modifier = modifier,
            )
    }
}

@Composable
private fun DiscoverGrid(
    items: List<MetaPreview>,
    isLoadingMore: Boolean,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val windowSize = LocalWindowSize.current
    // `rememberUpdatedState` keeps the count current inside the derived state;
    // capturing `items` directly would freeze it at the first composition and
    // make `shouldLoadMore` permanently true, loading every page at once. The
    // count is of the filtered list, so a narrow filter keeps pulling pages
    // until it has enough to show.
    val itemCount by rememberUpdatedState(items.size)
    val shouldLoadMore by remember {
        derivedStateOf {
            val last =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            last >= itemCount - 8
        }
    }
    LaunchedEffect(shouldLoadMore, items.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(posterSize.cellWidth(windowSize)),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(windowSize.screenPadding),
        verticalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
        horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
    ) {
        items(items, key = { "${it.type}:${it.id}" }) { item ->
            Box(Modifier.aspectRatio(POSTER_CARD_ASPECT)) {
                MetaPosterCard(
                    preview = item,
                    onTap = { onOpenDetail(LibraryItem.fromPreview(item)) },
                )
            }
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(AppSpacing.lg), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
