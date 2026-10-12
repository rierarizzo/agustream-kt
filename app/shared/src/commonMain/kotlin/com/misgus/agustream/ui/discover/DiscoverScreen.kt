package com.misgus.agustream.ui.discover

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * Discover section: everything the addon catalogs return, in one infinite grid.
 *
 * A single mixed grid (like the Library) instead of per-catalog rails: the
 * catalog boundaries are an addon detail the user should not have to care
 * about. Reaching the end loads the next page.
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    navigator: ContentNavigator,
    onOpenSettings: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    DiscoverContent(
        state = state,
        onOpenSettings = onOpenSettings,
        onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
        onLoadMore = viewModel::loadMore,
    )
}

@Composable
private fun DiscoverContent(
    state: DiscoverUiState,
    onOpenSettings: () -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
    onLoadMore: () -> Unit,
) {
    when {
        !state.isSignedIn ->
            Message(
                icon = Icons.Outlined.Lock,
                title = "Not signed in",
                message = "Sign in to discover your addon catalogs.",
                actionLabel = "Sign in",
                onAction = onOpenSettings,
            )
        state.isLoading && !state.hasLoaded ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && state.items.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load Discover",
                message = state.error.toUserMessage(),
            )
        state.items.isEmpty() ->
            Message(
                icon = Icons.Outlined.Explore,
                title = "Nothing to discover",
                message = "Install a metadata addon with catalogs to fill this section.",
            )
        else ->
            DiscoverGrid(state = state, onOpenDetail = onOpenDetail, onLoadMore = onLoadMore)
    }
}

@Composable
private fun DiscoverGrid(
    state: DiscoverUiState,
    onOpenDetail: (LibraryItem) -> Unit,
    onLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    // `rememberUpdatedState` keeps the count current inside the derived state;
    // capturing `state` directly would freeze it at the first composition and
    // make `shouldLoadMore` permanently true, loading every page at once.
    val itemCount by rememberUpdatedState(state.items.size)
    val shouldLoadMore by remember {
        derivedStateOf {
            val last =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            last >= itemCount - 8
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size, state.hasMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(230.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items(state.items, key = { "${it.type}:${it.id}" }) { item ->
            Box(Modifier.aspectRatio(0.58f)) {
                MetaPosterCard(
                    preview = item,
                    onTap = { onOpenDetail(LibraryItem.fromPreview(item)) },
                )
            }
        }
        if (state.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(AppSpacing.lg), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
