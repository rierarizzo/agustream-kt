package com.misgus.agustream.data.store

import com.misgus.agustream.core.watched.CachedSeriesState
import com.misgus.agustream.core.watched.WatchedSeriesCache
import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.platform.ioDispatcher
import com.misgus.agustream.data.stringOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import com.misgus.agustream.data.boolOrNull

/**
 * [WatchedSeriesCache] stored as a JSON file.
 *
 * Shape: `{ "<profileKey>": { "<seriesId>": { "s": "<signature>", "w": true } } }`.
 * Read and writes are best effort: a broken file degrades to "no cache" (so the
 * series are recomputed) instead of failing.
 */
class FileWatchedSeriesCache(
    private val path: String = AppPaths.watchedSeriesCacheFile,
) : WatchedSeriesCache {
    private var cache: JsonObject? = null

    private suspend fun load(): JsonObject = cache ?: withContext(ioDispatcher) {
        val store = JsonMapStore(path)
        store.read().also { cache = it }
    }

    private suspend fun save() {
        val data = cache ?: return
        JsonMapStore(path).write(data)
    }

    override suspend fun read(profileKey: String, seriesId: String): CachedSeriesState? {
        val profile = load()[profileKey]?.objOrNull() ?: return null
        val entry = profile[seriesId]?.objOrNull() ?: return null
        val signature = entry["s"].stringOrNull() ?: return null
        val watched = entry["w"].boolOrNull() ?: return null
        return CachedSeriesState(signature, watched)
    }

    override suspend fun write(profileKey: String, seriesId: String, state: CachedSeriesState) =
        writeAll(profileKey, mapOf(seriesId to state))

    override suspend fun writeAll(profileKey: String, states: Map<String, CachedSeriesState>) {
        if (states.isEmpty()) return
        val data = load().toMutableMap()
        val profile = data[profileKey]?.objOrNull()?.toMutableMap() ?: mutableMapOf()
        for ((seriesId, state) in states) {
            profile[seriesId] = JsonObject(
                mapOf(
                    "s" to JsonPrimitive(state.signature),
                    "w" to JsonPrimitive(state.watched),
                ),
            )
        }
        data[profileKey] = JsonObject(profile)
        cache = JsonObject(data)
        save()
    }

    override suspend fun clear(profileKey: String) {
        val data = load().toMutableMap()
        data.remove(profileKey)
        cache = JsonObject(data)
        save()
    }
}
