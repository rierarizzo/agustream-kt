package com.misgus.agustream.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable state of a full catalog grid. */
data class CatalogUiState(
    val items: List<MetaPreview> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasLoaded: Boolean = false,
    val hasMore: Boolean = true,
    val error: Throwable? = null,
)

/** Loads a full catalog, page by page. */
class CatalogViewModel(
    private val catalogs: CatalogRepository,
    val ref: CatalogRef,
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    /** Loads the first page. Does nothing when already loaded, unless [force]. */
    fun load(force: Boolean = false) {
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val items = catalogs.items(ref)
                _state.update {
                    it.copy(
                        items = items,
                        hasMore = items.isNotEmpty(),
                        hasLoaded = true,
                        isLoading = false,
                    )
                }
            } catch (error: Throwable) {
                _state.update { it.copy(isLoading = false, error = error) }
            }
        }
    }

    /**
     * Loads the next page and appends the new items.
     *
     * Stops when the addon returns nothing new, so a catalog that ignores `skip`
     * does not loop forever.
     */
    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasLoaded || !current.hasMore) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            try {
                val page = catalogs.items(ref, mapOf("skip" to current.items.size.toString()))
                val seen = current.items.map { it.id }.toMutableSet()
                val fresh = page.filter { seen.add(it.id) }
                _state.update {
                    if (fresh.isEmpty()) it.copy(isLoadingMore = false, hasMore = false)
                    else it.copy(items = it.items + fresh, isLoadingMore = false)
                }
            } catch (_: Throwable) {
                _state.update { it.copy(isLoadingMore = false, hasMore = false) }
            }
        }
    }
}
