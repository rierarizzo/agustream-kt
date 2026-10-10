package com.misgus.agustream.data.local

import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.data.backend.libraryItemFromRow
import com.misgus.agustream.data.backend.libraryItemToRow
import com.misgus.agustream.data.store.JsonFileStore
import com.misgus.agustream.data.stringOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * [LibraryRepository] backed by a local JSON file.
 *
 * Rows use the same shape as the backend table, so the shared mappers apply.
 */
class LocalLibraryRepository(private val store: JsonFileStore) : LibraryRepository {
    private val _changes = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = _changes.asStateFlow()

    override suspend fun all(): List<LibraryItem> =
        store.read()
            .map(::libraryItemFromRow)
            .sortedWith(compareByDescending { it.addedAt })

    override suspend fun contains(contentId: String): Boolean =
        store.read().any { it["content_id"].stringOrNull() == contentId }

    override suspend fun add(item: LibraryItem) {
        val rows = store.read()
        if (rows.any { it["content_id"].stringOrNull() == item.contentId }) return
        val row = libraryItemToRow(item)
        val withId = JsonObject(row + ("id" to JsonPrimitive(item.id.ifEmpty { item.contentId })))
        store.write(rows + withId)
        _changes.value += 1
    }

    override suspend fun remove(contentId: String) {
        val rows = store.read()
        store.write(rows.filterNot { it["content_id"].stringOrNull() == contentId })
        _changes.value += 1
    }
}
