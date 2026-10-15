package com.misgus.pengu.ui.streams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.pengu.core.model.Stream
import com.misgus.pengu.core.repository.StreamGroup
import com.misgus.pengu.core.repository.StreamRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One stream flattened with the addon that produced it. */
data class StreamEntry(
    val stream: Stream,
    val addonName: String,
)

/** Immutable state of the stream picker. */
data class StreamsUiState(
    val groups: List<StreamGroup> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: Throwable? = null,
) {
    /** Every stream, filtered by [query] against name, description, title and addon. */
    val entries: List<StreamEntry>
        get() {
            val all =
                groups.flatMap { group ->
                    group.streams.map { StreamEntry(it, group.addonName) }
                }
            if (query.isEmpty()) return all
            val q = query.lowercase()
            return all.filter { entry ->
                (entry.stream.name ?: "").lowercase().contains(q) ||
                    (entry.stream.description ?: "").lowercase().contains(q) ||
                    (entry.stream.title ?: "").lowercase().contains(q) ||
                    entry.addonName.lowercase().contains(q)
            }
        }

    val totalCount: Int get() = groups.sumOf { it.streams.size }
}

/** Loads the streams for one title or episode. */
class StreamsViewModel(
    private val streams: StreamRepository,
    private val type: String,
    private val id: String,
) : ViewModel() {
    private val _state = MutableStateFlow(StreamsUiState())
    val state: StateFlow<StreamsUiState> = _state.asStateFlow()

    fun setQuery(value: String) {
        _state.update { it.copy(query = value.trim()) }
    }

    fun load(force: Boolean = false) {
        if (_state.value.isLoading || (_state.value.hasLoaded && !force)) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, groups = emptyList()) }
            try {
                val groups = streams.all(type, id)
                _state.update { it.copy(isLoading = false, hasLoaded = true, groups = groups) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                _state.update { it.copy(isLoading = false, error = error) }
            }
        }
    }
}
