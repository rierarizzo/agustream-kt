package com.misgus.agustream.core.model

import kotlin.time.Duration
import kotlin.time.Instant

/** Playback progress for one title or episode. */
data class WatchProgress(
    val id: String,
    val contentId: String,
    val contentType: String,
    /** Episode video id, for series. */
    val videoId: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    /** Current playback position. */
    val position: Duration? = null,
    /** Total duration. */
    val duration: Duration? = null,
    /** When the title was last watched. */
    val lastWatched: Instant? = null,
    val progressKey: String? = null,
    val profileId: Int? = null,
) {
    /** Fraction watched, `0.0`–`1.0`, or `null` when the duration is unknown. */
    val fraction: Double?
        get() {
            val total = duration ?: return null
            val current = position ?: return null
            if (total.inWholeMilliseconds <= 0) return null
            return (current.inWholeMilliseconds.toDouble() / total.inWholeMilliseconds)
                .coerceIn(0.0, 1.0)
        }
}

/**
 * One entry of the account's watched history.
 *
 * A row with [season] and [episode] both `null` is a title-level marker (a
 * movie watched, or a series Nuvio marked as watched); a row with both set is
 * one episode.
 */
data class WatchedEntry(
    val contentId: String,
    /** `movie`, `series`, ... */
    val contentType: String,
    val season: Int? = null,
    val episode: Int? = null,
) {
    /** `true` for a title-level marker, `false` for an episode. */
    val isMarker: Boolean get() = season == null && episode == null
}
