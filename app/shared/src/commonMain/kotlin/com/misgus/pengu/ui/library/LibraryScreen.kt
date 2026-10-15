package com.misgus.pengu.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.empty_nothing_to_show
import com.misgus.pengu.app.shared.generated.resources.library_empty_message
import com.misgus.pengu.app.shared.generated.resources.library_error_title
import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.repository.PosterSize
import com.misgus.pengu.ui.components.Message
import com.misgus.pengu.ui.components.PosterTile
import com.misgus.pengu.ui.components.SkeletonPosterGrid
import com.misgus.pengu.ui.components.SyncingIndicator
import com.misgus.pengu.ui.components.toUserMessage
import com.misgus.pengu.ui.icons.AppIcons
import com.misgus.pengu.ui.services.LocalAppServices
import com.misgus.pengu.ui.shell.ContentNavigator
import com.misgus.pengu.ui.shell.ContentRoute
import com.misgus.pengu.ui.theme.AppSpacing
import com.misgus.pengu.ui.theme.LocalWindowSize
import com.misgus.pengu.ui.theme.POSTER_CARD_ASPECT
import com.misgus.pengu.ui.theme.cellWidth
import com.misgus.pengu.ui.theme.gridSpacing
import com.misgus.pengu.ui.theme.screenPadding
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
                icon = AppIcons.Outlined.ErrorOutline,
                title = stringResource(Res.string.library_error_title),
                message = stringResource(state.error.toUserMessage()),
            )
        state.items.isEmpty() ->
            Message(
                icon = AppIcons.Outlined.Inbox,
                title = stringResource(Res.string.empty_nothing_to_show),
                message = stringResource(Res.string.library_empty_message),
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
                    Box(Modifier.aspectRatio(POSTER_CARD_ASPECT)) {
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
