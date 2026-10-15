package com.misgus.pengu.core.repository

import com.misgus.pengu.core.model.WatchProgress
import com.misgus.pengu.core.model.WatchedEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * Read and write access to watch state: resume progress and watched markers.
 *
 * Observable — [changes] bumps after a write.
 */
interface ProgressRepository {
    /** Bumped when [all] may have changed (after a write). */
    val changes: StateFlow<Long>

    /** Every progress entry, most recently watched first. */
    suspend fun all(): List<WatchProgress>

    /** Creates or updates [progress], matched by [WatchProgress.progressKey]. */
    suspend fun save(progress: WatchProgress)

    /**
     * Watched history entries for [candidateIds].
     *
     * Mirrors the account's own watched data, not a client-side heuristic.
     */
    suspend fun watchedEntries(candidateIds: Collection<String>): List<WatchedEntry>

    /**
     * Marks a title (or one episode) as watched.
     *
     * With [season] and [episode] omitted it writes a title-level marker, which
     * is how a movie or a whole series is marked.
     */
    suspend fun markWatched(
        contentId: String,
        contentType: String,
        season: Int? = null,
        episode: Int? = null,
    )

    /** Removes a watched marker (the same identity [markWatched] writes). */
    suspend fun unmarkWatched(
        contentId: String,
        season: Int? = null,
        episode: Int? = null,
    )
}
