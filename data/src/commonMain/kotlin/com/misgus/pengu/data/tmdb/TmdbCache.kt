package com.misgus.pengu.data.tmdb

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A small in-memory cache with a time-to-live, keyed by a string.
 *
 * Every request it saves is one against the user's own TMDB quota, so the
 * detail payload and the recommendation list are cached for the life of the
 * process; an entry that outlives its TTL is dropped and re-fetched. It is
 * deliberately not persisted: TMDB data changes, and a stale cache would be
 * harder to explain than the extra request.
 */
internal class TmdbCache<V>(
    private val ttl: Duration,
) {
    private class Entry<V>(
        val value: V,
        val mark: TimeMark,
    )

    private val mutex = Mutex()
    private val entries = mutableMapOf<String, Entry<V>>()

    suspend fun get(key: String): V? =
        mutex.withLock {
            val entry = entries[key] ?: return@withLock null
            if (entry.mark.elapsedNow() > ttl) {
                entries.remove(key)
                null
            } else {
                entry.value
            }
        }

    suspend fun put(
        key: String,
        value: V,
    ) = mutex.withLock {
        entries[key] = Entry(value, TimeSource.Monotonic.markNow())
    }
}
