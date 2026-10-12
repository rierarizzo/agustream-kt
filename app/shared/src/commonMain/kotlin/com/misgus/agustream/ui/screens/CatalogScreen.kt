package com.misgus.agustream.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.ui.catalog.CatalogViewModel
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.SkeletonPosterGrid
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppSpacing

/**
 * Full catalog grid, opened from a Home row's "See all".
 *
 * Scrolls to the bottom to load more pages (`skip`).
 */
@Composable
fun CatalogScreen(
    ref: CatalogRef,
    onBack: () -> Unit,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    val services = LocalAppServices.current
    val viewModel: CatalogViewModel =
        viewModel(key = "catalog-${ref.key}") {
            CatalogViewModel(services.catalogs, ref)
        }
    LaunchedEffect(ref) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    // Keep the count current inside the derived state, or it freezes at the
    // first composition and every page loads at once.
    val itemCount by rememberUpdatedState(state.items.size)
    val shouldLoadMore by remember {
        derivedStateOf {
            val last =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            last >= itemCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = AppSpacing.xl, top = AppSpacing.xl, end = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.size(AppSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    ref.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(ref.addonName, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        Box(Modifier.weight(1f)) {
            CatalogContent(state, gridState, onOpenDetail)
        }
    }
}

@Composable
private fun CatalogContent(
    state: com.misgus.agustream.ui.catalog.CatalogUiState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    when {
        state.isLoading && !state.hasLoaded -> SkeletonPosterGrid()
        state.error != null && !state.hasLoaded ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load this catalog",
                message = "Try again later.",
            )
        state.items.isEmpty() ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = "Nothing to show",
                message = "This catalog is empty.",
            )
        else ->
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(230.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = AppSpacing.xl, end = AppSpacing.xl, bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                items(state.items, key = { it.id }) { item ->
                    Box(Modifier.aspectRatio(0.58f)) {
                        MetaPosterCard(
                            preview = item,
                            onTap = { onOpenDetail(LibraryItem.fromPreview(item)) },
                        )
                    }
                }
                if (state.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    }
                }
            }
    }
}
