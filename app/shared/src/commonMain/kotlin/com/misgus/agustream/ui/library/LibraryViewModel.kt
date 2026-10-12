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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable state of the Library section. */
data class LibraryUiState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
    val items: List<LibraryItem> = emptyList(),
    val progressByContentId: Map<String, WatchProgress> = emptyMap(),
    val watchedIds: Set<String> = emptySet(),
    val isSyncingWatched: Boolean = false,
)

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
                when {
                    !accountState.isSignedIn -> clear()
                    // Wait for a profile before loading: the library is scoped to
                    // one, so a load without it would just fail.
                    accountState.requiresProfile && accountState.activeProfile == null -> Unit
                    else -> load(force = true)
                }
            }
        }
        // A write elsewhere (e.g. "add to favorites" from the detail screen)
        // must refresh the grid, so the repositories are observable too.
        viewModelScope.launch { library.changes.drop(1).collect { load(force = true) } }
        viewModelScope.launch { progress.changes.drop(1).collect { load(force = true) } }
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
        _state.update { it.copy(isSignedIn = true, isLoading = true, error = null) }
        var progressEntries: List<WatchProgress> = emptyList()
        var loaded = false
        try {
            // The library and the progress that decorates it are independent; a
            // single round-trip each, run together.
            val (items, entries) =
                coroutineScope {
                    val libraryDeferred = async { library.all() }
                    val progressDeferred = async { progress.all() }
                    libraryDeferred.await() to progressDeferred.await()
                }
            progressEntries = entries
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
