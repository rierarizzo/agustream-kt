package com.misgus.agustream.ui.screens

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_back
import agustream.app.shared.generated.resources.catalog_empty_message
import agustream.app.shared.generated.resources.catalog_error_title
import agustream.app.shared.generated.resources.empty_nothing_to_show
import agustream.app.shared.generated.resources.error_try_again_later
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.catalog.CatalogViewModel
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.SkeletonPosterGrid
import com.misgus.agustream.ui.components.sectionHeading
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.safeBottomInset
import com.misgus.agustream.ui.theme.screenPadding
import org.jetbrains.compose.resources.stringResource

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
    val settingsRepository = services.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val windowSize = LocalWindowSize.current

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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(
            modifier = Modifier.padding(start = windowSize.screenPadding, top = windowSize.screenPadding, end = windowSize.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
            }
            Spacer(Modifier.size(AppSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    ref.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.sectionHeading(),
                )
                Text(ref.addonName, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        Box(Modifier.weight(1f)) {
            CatalogContent(state, settings.posterSize, gridState, onOpenDetail)
        }
    }
}

@Composable
private fun CatalogContent(
    state: com.misgus.agustream.ui.catalog.CatalogUiState,
    posterSize: PosterSize,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    val windowSize = LocalWindowSize.current
    when {
        state.isLoading && !state.hasLoaded -> SkeletonPosterGrid(posterSize)
        state.error != null && !state.hasLoaded ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = stringResource(Res.string.catalog_error_title),
                message = stringResource(Res.string.error_try_again_later),
            )
        state.items.isEmpty() ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = stringResource(Res.string.empty_nothing_to_show),
                message = stringResource(Res.string.catalog_empty_message),
            )
        else ->
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(posterSize.cellWidth(windowSize)),
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = windowSize.screenPadding,
                        end = windowSize.screenPadding,
                        bottom = windowSize.screenPadding + safeBottomInset,
                    ),
                verticalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
                horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
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
