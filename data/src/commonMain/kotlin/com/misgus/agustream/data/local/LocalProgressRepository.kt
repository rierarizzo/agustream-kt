package com.misgus.agustream.data.local

import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.model.WatchedEntry
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.data.backend.watchProgressFromRow
import com.misgus.agustream.data.backend.watchProgressToRow
import com.misgus.agustream.data.intOrNull
import com.misgus.agustream.data.store.JsonFileStore
import com.misgus.agustream.data.stringOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock

/**
 * [ProgressRepository] backed by local JSON files.
 *
 * Progress is deduped by `progress_key` (falling back to `content_id` +
 * `video_id`). Watched markers live in a second file; when it is missing,
 * completed progress is still reported as watched.
 */
class LocalProgressRepository(
    private val store: JsonFileStore,
    private val watched: JsonFileStore? = null,
) : ProgressRepository {
    private val _changes = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = _changes.asStateFlow()

    override suspend fun all(): List<WatchProgress> =
        store.read()
            .map(::watchProgressFromRow)
            .sortedWith(compareByDescending { it.lastWatched })

    override suspend fun watchedEntries(candidateIds: Collection<String>): List<WatchedEntry> {
        val candidates = candidateIds.toSet()
        if (candidates.isEmpty()) return emptyList()
        val result = mutableListOf<WatchedEntry>()

        watched?.read()?.forEach { row ->
            val contentId = row["content_id"].stringOrNull() ?: return@forEach
            if (contentId !in candidates) return@forEach
            result += WatchedEntry(
                contentId = contentId,
                contentType = row["content_type"].stringOrNull() ?: "",
                season = row["season"].intOrNull(),
                episode = row["episode"].intOrNull(),
            )
        }

        store.read().map(::watchProgressFromRow).forEach { entry ->
            if (entry.contentId !in candidates) return@forEach
            if ((entry.fraction ?: 0.0) < 0.9) return@forEach
            result += WatchedEntry(
                contentId = entry.contentId,
                contentType = entry.contentType,
                season = entry.season,
                episode = entry.episode,
            )
        }
        return result
    }

    override suspend fun save(progress: WatchProgress) {
        val rows = store.read()
        val key = keyOfProgress(progress)
        val row = watchProgressToRow(progress)
        val withId = JsonObject(row + ("id" to JsonPrimitive(progress.id.ifEmpty { key })))
        store.write(rows.filterNot { keyOfRow(it) == key } + withId)
        _changes.value += 1
    }

    override suspend fun markWatched(
        contentId: String,
        contentType: String,
        season: Int?,
        episode: Int?,
    ) {
        val target = watched ?: return
        val key = markerKey(contentId, season, episode)
        val rows = target.read()
        val newRow = buildMap<String, kotlinx.serialization.json.JsonElement> {
            put("content_id", JsonPrimitive(contentId))
            put("content_type", JsonPrimitive(contentType))
            put("watched_at", JsonPrimitive(Clock.System.now().toEpochMilliseconds()))
            season?.let { put("season", JsonPrimitive(it)) }
            episode?.let { put("episode", JsonPrimitive(it)) }
        }
        target.write(rows.filterNot { markerKeyOfRow(it) == key } + JsonObject(newRow))
        _changes.value += 1
    }

    override suspend fun unmarkWatched(contentId: String, season: Int?, episode: Int?) {
        val target = watched ?: return
        val key = markerKey(contentId, season, episode)
        target.write(target.read().filterNot { markerKeyOfRow(it) == key })
        _changes.value += 1
    }

    private fun keyOfProgress(progress: WatchProgress): String {
        val explicit = progress.progressKey
        if (!explicit.isNullOrEmpty()) return explicit
        return "${progress.contentId}|${progress.videoId ?: ""}"
    }

    private fun keyOfRow(row: JsonObject): String {
        val explicit = row["progress_key"].stringOrNull()
        if (!explicit.isNullOrEmpty()) return explicit
        return "${row["content_id"].stringOrNull()}|${row["video_id"].stringOrNull() ?: ""}"
    }

    private fun markerKey(contentId: String, season: Int?, episode: Int?) =
        "$contentId|${season ?: -1}|${episode ?: -1}"

    private fun markerKeyOfRow(row: JsonObject) = markerKey(
        row["content_id"].stringOrNull() ?: "",
        row["season"].intOrNull(),
        row["episode"].intOrNull(),
    )
}
