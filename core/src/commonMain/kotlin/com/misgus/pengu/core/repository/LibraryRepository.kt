package com.misgus.pengu.core.repository

import com.misgus.pengu.core.model.LibraryItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Read and write access to the saved library.
 *
 * Observable — [changes] bumps after an add or a remove so screens can reload.
 */
interface LibraryRepository {
    /** Bumped when [all] may have changed (after an add or a remove). */
    val changes: StateFlow<Long>

    /** Every saved title, newest first. */
    suspend fun all(): List<LibraryItem>

    /** Whether [contentId] is already saved. */
    suspend fun contains(contentId: String): Boolean

    /** Saves [item]. Saving a title that is already saved is a no-op. */
    suspend fun add(item: LibraryItem)

    /** Removes the title with [contentId], if present. */
    suspend fun remove(contentId: String)
}
