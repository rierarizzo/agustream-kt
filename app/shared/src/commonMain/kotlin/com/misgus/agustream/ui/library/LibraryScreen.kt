package com.misgus.agustream.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.PosterTile
import com.misgus.agustream.ui.components.SkeletonPosterGrid
import com.misgus.agustream.ui.components.SyncingIndicator
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.screenPadding
import org.jetbrains.compose.resources.stringResource

/** Library section: the grid of saved titles. */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    navigator: ContentNavigator,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsRepository = LocalAppServices.current.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        LibraryContent(
            state = state,
            posterSize = settings.libraryPosterSize,
            onOpen = { navigator.push(ContentRoute.Detail(it, knownSaved = true)) },
        )
        if (state.isSyncingWatched) {
            Box(Modifier.align(Alignment.TopEnd).padding(AppSpacing.xl)) {
                SyncingIndicator()
            }
        }
    }
}

@Composable
private fun LibraryContent(
    state: LibraryUiState,
    posterSize: PosterSize,
    onOpen: (LibraryItem) -> Unit,
) {
    val windowSize = LocalWindowSize.current
    when {
        state.isLoading && !state.hasLoaded -> SkeletonPosterGrid(posterSize)
        state.error != null && !state.hasLoaded ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Could not load the library",
                message = stringResource(state.error.toUserMessage()),
            )
        state.items.isEmpty() ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = "Nothing to show",
                message = "Your library is empty.",
            )
        else ->
            // The grid owns the padding through contentPadding: the scroll area
            // reaches the window edges, so the last row is never clipped.
            LazyVerticalGrid(
                columns = GridCells.Adaptive(posterSize.cellWidth(windowSize)),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(windowSize.screenPadding),
                verticalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
                horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
            ) {
                items(state.items, key = { it.contentId }) { item ->
                    Box(Modifier.aspectRatio(0.58f)) {
                        PosterTile(
                            item = item,
                            progress = state.progressByContentId[item.contentId],
                            watched = item.contentId in state.watchedIds,
                            onTap = { onOpen(item) },
                        )
                    }
                }
            }
    }
}
