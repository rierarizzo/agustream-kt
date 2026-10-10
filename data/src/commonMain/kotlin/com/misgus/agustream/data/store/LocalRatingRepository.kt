package com.misgus.agustream.data.store

import com.misgus.agustream.core.repository.RatingRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * [RatingRepository] backed by a local JSON file (`{contentId: rating}`).
 *
 * A mutex serializes the read-modify-write cycle.
 */
class LocalRatingRepository(
    private val store: JsonMapStore,
) : RatingRepository {
    private val mutex = Mutex()

    override suspend fun ratingOf(contentId: String): Int? =
        mutex.withLock {
            (store.read()[contentId] as? JsonPrimitive)?.content?.toIntOrNull()
        }

    override suspend fun setRating(
        contentId: String,
        rating: Int?,
    ) = mutex.withLock {
        val data = store.read().toMutableMap()
        if (rating == null) {
            data.remove(contentId)
        } else {
            data[contentId] = JsonPrimitive(rating)
        }
        store.write(JsonObject(data))
    }
}
