package com.misgus.pengu.ui.common

import com.misgus.pengu.core.model.WatchProgress
import com.misgus.pengu.core.repository.AccountRepository
import com.misgus.pengu.core.util.runCatchingCancellable
import com.misgus.pengu.core.watched.WatchedBadgeResolver
import com.misgus.pengu.core.watched.WatchedCandidate

/**
 * Resolves watched badges for a screen, ignoring stale results.
 *
 * A resolve is slow (it may fetch metadata for many series) and a newer load can
 * start before it finishes, so each call takes a ticket: only the newest one is
 * allowed to publish. The profile key scopes the on-disk cache and is derived
 * here, so every screen uses the same one.
 */
class WatchedBadgeSync(
    private val badges: WatchedBadgeResolver,
    private val account: AccountRepository,
) {
    private var request = 0

    /** Drops any in-flight result, so it cannot publish over a cleared screen. */
    fun invalidate() {
        request++
    }

    /**
     * Resolves [candidates] and reports the watched ids.
     *
     * [onStart] runs before the lookup (to show a "syncing" indicator) and
     * [onResult] only when this call is still the newest one.
     */
    suspend fun resolve(
        candidates: Collection<WatchedCandidate>,
        progressEntries: List<WatchProgress>?,
        onStart: () -> Unit,
        onResult: (Set<String>) -> Unit,
    ) {
        if (candidates.isEmpty()) return
        val current = ++request
        onStart()
        val watched =
            runCatchingCancellable {
                badges.resolve(
                    candidates = candidates,
                    progressEntries = progressEntries,
                    profileKey = profileKey(),
                )
            }.getOrDefault(emptySet())
        if (current != request) return
        onResult(watched)
    }

    private fun profileKey() =
        account.state.value.activeProfile
            ?.profileId
            ?.toString() ?: ""
}
