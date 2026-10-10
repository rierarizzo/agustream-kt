package com.misgus.agustream.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.core.util.runCatchingCancellable
import com.misgus.agustream.core.watched.WatchedBadgeResolver
import com.misgus.agustream.core.watched.WatchedCandidate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Kind of titles the library grid shows. */
enum class LibraryFilter(
    val label: String,
) {
    ALL("All"),
    MOVIES("Movies"),
    SHOWS("Shows"),
}

/** Immutable state of the Library section. */
data class LibraryUiState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
    val items: List<LibraryItem> = emptyList(),
    val progressByContentId: Map<String, WatchProgress> = emptyMap(),
    val filter: LibraryFilter = LibraryFilter.ALL,
    val watchedIds: Set<String> = emptySet(),
    val isSyncingWatched: Boolean = false,
) {
    /** Titles matching the active [filter]. */
    val visibleItems: List<LibraryItem>
        get() =
            when (filter) {
                LibraryFilter.ALL -> items
                LibraryFilter.MOVIES -> items.filter { it.contentType == "movie" }
                LibraryFilter.SHOWS -> items.filter { it.contentType == "series" }
            }
}

/**
 * Loads the library and the watch progress that decorates it.
 *
 * The watched badges are resolved in the background, so the grid paints as soon
 * as the content is there.
 */
class LibraryViewModel(
    private val account: AccountRepository,
    private val library: LibraryRepository,
    private val progress: ProgressRepository,
    private val badges: WatchedBadgeResolver,
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    private var watchedRequest = 0

    init {
        viewModelScope.launch {
            account.state.collect { accountState ->
                if (accountState.isSignedIn) load(force = true) else clear()
            }
        }
        // A write elsewhere (e.g. "add to favorites" from the detail screen)
        // must refresh the grid, so the repositories are observable too.
        viewModelScope.launch { library.changes.drop(1).collect { load(force = true) } }
        viewModelScope.launch { progress.changes.drop(1).collect { load(force = true) } }
    }

    fun setFilter(filter: LibraryFilter) {
        if (filter == _state.value.filter) return
        _state.update { it.copy(filter = filter) }
    }

    /** Loads library and progress. Does nothing when already loaded, unless [force]. */
    fun load(force: Boolean = false) {
        if (!account.state.value.isSignedIn) {
            clear()
            return
        }
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch { doLoad() }
    }

    private suspend fun doLoad() {
        _state.update { it.copy(isLoading = true, error = null) }
        var progressEntries: List<WatchProgress> = emptyList()
        var loaded = false
        try {
            val items = library.all()
            progressEntries = progress.all()
            _state.update {
                it.copy(
                    isLoading = false,
                    hasLoaded = true,
                    items = items,
                    progressByContentId = latestByContentId(progressEntries),
                )
            }
            loaded = true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            _state.update { it.copy(isLoading = false, error = error) }
        }
        if (loaded) syncWatched(progressEntries)
    }

    private suspend fun syncWatched(progressEntries: List<WatchProgress>) {
        val request = ++watchedRequest
        val candidates = _state.value.items.map { WatchedCandidate(it.contentId, it.contentType) }
        if (candidates.isEmpty()) return
        _state.update { it.copy(isSyncingWatched = true) }
        val watched =
            runCatchingCancellable {
                badges.resolve(
                    candidates = candidates,
                    progressEntries = progressEntries,
                    profileKey =
                        account.state.value.activeProfile
                            ?.profileId
                            ?.toString() ?: "",
                )
            }.getOrDefault(emptySet())
        if (request != watchedRequest) return
        _state.update { it.copy(watchedIds = watched, isSyncingWatched = false) }
    }

    private fun clear() {
        watchedRequest++
        _state.value = LibraryUiState()
    }

    /**
     * Keeps the most recent progress entry per content id.
     *
     * Series have one row per episode, so the newest one describes what the user
     * is currently watching.
     */
    private fun latestByContentId(entries: List<WatchProgress>): Map<String, WatchProgress> {
        val latest = mutableMapOf<String, WatchProgress>()
        for (entry in entries) {
            val current = latest[entry.contentId]
            val left = entry.lastWatched
            val right = current?.lastWatched
            if (current == null || (left != null && (right == null || left > right))) {
                latest[entry.contentId] = entry
            }
        }
        return latest
    }
}
