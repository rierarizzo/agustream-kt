package com.misgus.agustream.data.backend

import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [LibraryRepository] backed by a Nuvio (Supabase) deployment.
 *
 * Reads and writes are scoped to the active profile: `library_items` belongs to
 * a profile, and row-level security only stops at the account.
 */
class NuvioLibraryRepository(
    private val rest: NuvioRest,
    private val auth: NuvioAuth,
    private val account: AccountRepository,
) : LibraryRepository {
    private val _changes = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = _changes.asStateFlow()

    override suspend fun all(): List<LibraryItem> {
        val rows = rest.select("library_items", order = "added_at.desc", filter = profileFilter)
        return rows.map(::libraryItemFromRow)
    }

    override suspend fun contains(contentId: String): Boolean {
        val rows =
            rest.select(
                "library_items",
                filter = "$profileFilter&content_id=eq.${postgrestLiteral(contentId)}",
            )
        return rows.isNotEmpty()
    }

    override suspend fun add(item: LibraryItem) {
        // Guarded instead of `upsert`: it does not depend on the backend having
        // a unique constraint on `(profile_id, content_id)`.
        if (contains(item.contentId)) return
        rest.insert(
            "library_items",
            libraryItemToRow(item, profileId = requireProfileId(), userId = requireUserId()),
        )
        _changes.value += 1
    }

    override suspend fun remove(contentId: String) {
        rest.delete("library_items", filter = "$profileFilter&content_id=eq.${postgrestLiteral(contentId)}")
        _changes.value += 1
    }

    private val profileFilter: String get() = "profile_id=eq.${requireProfileId()}"

    private fun requireProfileId(): Int = account.requireProfileId()

    private fun requireUserId(): String {
        val userId = auth.session?.userId
        if (userId.isNullOrEmpty()) throw BackendException("Not signed in")
        return userId
    }
}
