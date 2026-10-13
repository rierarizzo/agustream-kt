package com.misgus.agustream.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.PosterSize
import com.misgus.agustream.ui.components.Message
import com.misgus.agustream.ui.components.MetaPosterCard
import com.misgus.agustream.ui.components.SectionHeader
import com.misgus.agustream.ui.components.toUserMessage
import com.misgus.agustream.ui.discover.DiscoverFeed
import com.misgus.agustream.ui.discover.DiscoverViewModel
import com.misgus.agustream.ui.services.LocalAppServices
import com.misgus.agustream.ui.shell.ContentNavigator
import com.misgus.agustream.ui.shell.ContentRoute
import com.misgus.agustream.ui.theme.AppSpacing
import com.misgus.agustream.ui.theme.LocalWindowSize
import com.misgus.agustream.ui.theme.cellWidth
import com.misgus.agustream.ui.theme.gridSpacing
import com.misgus.agustream.ui.theme.railHeight
import com.misgus.agustream.ui.theme.screenPadding

/** Which title types the search results and the discover feed show. */
enum class MediaTypeFilter(
    val label: String,
) {
    ALL("All Types"),
    MOVIES("Movies"),
    SERIES("Series"),
    ;

    /** `true` when [preview] belongs to the selected type. */
    fun matches(preview: MetaPreview): Boolean =
        when (this) {
            ALL -> true
            MOVIES -> preview.type == "movie"
            SERIES -> preview.type == "series"
        }
}

/**
 * Search section: a free-text search across every searchable addon catalog,
 * with the discover feed underneath the bar while the query is empty.
 *
 * Discover used to be its own section; it lives here now. Two dropdowns — the
 * title type and the genre — narrow both the feed and the search results.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    discoverViewModel: DiscoverViewModel,
    navigator: ContentNavigator,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val discoverState by discoverViewModel.state.collectAsStateWithLifecycle()
    val settingsRepository = LocalAppServices.current.settings
    val settings by settingsRepository.state.collectAsStateWithLifecycle()
    var typeFilter by remember { mutableStateOf(MediaTypeFilter.ALL) }
    var genreFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { discoverViewModel.load() }

    // Genre options come from what has actually loaded, so the menu never lists
    // a genre the addons do not return.
    val genres =
        remember(discoverState.items, state.results) {
            (discoverState.items + state.results)
                .flatMap { it.genres }
                .distinct()
                .sorted()
        }
    val matches: (MetaPreview) -> Boolean = { preview ->
        typeFilter.matches(preview) && (genreFilter == null || genreFilter in preview.genres)
    }

    Column(Modifier.fillMaxSize()) {
        SearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
        if (state.query.isBlank()) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineSmall,
                modifier =
                    Modifier.padding(
                        start = LocalWindowSize.current.screenPadding,
                        end = LocalWindowSize.current.screenPadding,
                        top = AppSpacing.lg,
                    ),
            )
        }
        FilterRow(
            typeFilter = typeFilter,
            onTypeSelect = { typeFilter = it },
            genres = genres,
            genreFilter = genreFilter,
            onGenreSelect = { genreFilter = it },
        )
        Box(Modifier.weight(1f)) {
            if (state.query.isBlank()) {
                DiscoverFeed(
                    state = discoverState,
                    items = discoverState.items.filter(matches),
                    posterSize = settings.posterSize,
                    onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
                    onLoadMore = discoverViewModel::loadMore,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                SearchResults(
                    state = state,
                    matches = matches,
                    emptyMessage = emptyMessage(state, typeFilter, genreFilter),
                    posterSize = settings.posterSize,
                    onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text("Search movies and series") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = LocalWindowSize.current.screenPadding,
                    end = LocalWindowSize.current.screenPadding,
                    top = AppSpacing.lg,
                ),
    )
}

/** The type and genre dropdowns, shared by the discover feed and the results. */
@Composable
private fun FilterRow(
    typeFilter: MediaTypeFilter,
    onTypeSelect: (MediaTypeFilter) -> Unit,
    genres: List<String>,
    genreFilter: String?,
    onGenreSelect: (String?) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = LocalWindowSize.current.screenPadding, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterDropdown(
            options = MediaTypeFilter.entries,
            selected = typeFilter,
            optionLabel = { it.label },
            onSelect = onTypeSelect,
        )
        FilterDropdown(
            options = listOf<String?>(null) + genres,
            selected = genreFilter,
            optionLabel = { it ?: "All genres" },
            onSelect = onGenreSelect,
        )
    }
}

/** A compact Material outlined dropdown: it shows the selection and opens a menu. */
@Composable
private fun <T> FilterDropdown(
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        ) {
            Text(
                text = optionLabel(selected),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    matches: (MetaPreview) -> Boolean,
    emptyMessage: String,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    when {
        state.isSearching && state.results.isEmpty() ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        state.error != null && state.results.isEmpty() ->
            Message(
                icon = Icons.Outlined.ErrorOutline,
                title = "Search failed",
                message = state.error.toUserMessage(),
            )
        state.results.none(matches) && state.hasSearched ->
            Message(
                icon = Icons.Outlined.Inbox,
                title = "No results",
                message = emptyMessage,
            )
        else -> {
            val visible = state.results.filter(matches)
            val movies = visible.filter { it.type == "movie" }
            val series = visible.filter { it.type == "series" }
            val other = visible.filter { it.type != "movie" && it.type != "series" }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                if (movies.isNotEmpty()) {
                    SectionHeader("Movies")
                    ResultsRail(movies, posterSize, onOpenDetail)
                }
                if (series.isNotEmpty()) {
                    SectionHeader("Series")
                    ResultsRail(series, posterSize, onOpenDetail)
                }
                if (other.isNotEmpty()) {
                    SectionHeader("Other")
                    ResultsRail(other, posterSize, onOpenDetail)
                }
                Spacer(Modifier.height(AppSpacing.xl))
            }
        }
    }
}

private fun emptyMessage(
    state: SearchUiState,
    typeFilter: MediaTypeFilter,
    genre: String?,
): String {
    val query = state.query.trim()
    if (typeFilter == MediaTypeFilter.ALL && genre == null) return "Nothing matched \"$query\"."
    val type =
        when (typeFilter) {
            MediaTypeFilter.ALL -> "titles"
            MediaTypeFilter.MOVIES -> "movies"
            MediaTypeFilter.SERIES -> "series"
        }
    val inGenre = if (genre != null) " in $genre" else ""
    return "No $type$inGenre matched \"$query\"."
}

@Composable
private fun ResultsRail(
    previews: List<MetaPreview>,
    posterSize: PosterSize,
    onOpenDetail: (LibraryItem) -> Unit,
) {
    val windowSize = LocalWindowSize.current
    LazyRow(
        modifier = Modifier.height(posterSize.railHeight(windowSize)),
        contentPadding = PaddingValues(horizontal = windowSize.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(windowSize.gridSpacing),
    ) {
        items(previews, key = { "${it.type}:${it.id}" }) { item ->
            MetaPosterCard(
                preview = item,
                width = posterSize.cellWidth(windowSize),
                onTap = { onOpenDetail(LibraryItem.fromPreview(item)) },
            )
        }
    }
}
