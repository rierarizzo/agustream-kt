package com.misgus.pengu.core.repository

/**
 * Stores the user's 1–10 rating for a title, locally.
 *
 * Nuvio's backend has no per-title rating column, so a rating is not synced
 * across devices: it lives on this machine.
 */
interface RatingRepository {
    /** Rating for [contentId], or `null` when unrated. */
    suspend fun ratingOf(contentId: String): Int?

    /** Sets the rating for [contentId], or clears it when [rating] is `null`. */
    suspend fun setRating(
        contentId: String,
        rating: Int?,
    )
}

/** Repository that stores no ratings (used when running without a store). */
class NoRatingRepository : RatingRepository {
    override suspend fun ratingOf(contentId: String): Int? = null

    override suspend fun setRating(
        contentId: String,
        rating: Int?,
    ) = Unit
}
