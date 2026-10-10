package com.misgus.agustream.data.store

import com.misgus.agustream.core.repository.RatingRepository
import kotlinx.serialization.json.JsonPrimitive

/** [RatingRepository] backed by a local JSON file (`{contentId: rating}`). */
class LocalRatingRepository(private val store: JsonMapStore) : RatingRepository {
    override suspend fun ratingOf(contentId: String): Int? =
        (store.read()[contentId] as? JsonPrimitive)?.content?.toIntOrNull()

    override suspend fun setRating(contentId: String, rating: Int?) {
        val data = store.read().toMutableMap()
        if (rating == null) {
            data.remove(contentId)
        } else {
            data[contentId] = JsonPrimitive(rating)
        }
        store.write(kotlinx.serialization.json.JsonObject(data))
    }
}
