package com.misgus.agustream.core.model

/**
 * Names of the `extra` parameters a Stremio catalog can declare.
 *
 * They are protocol vocabulary, not UI labels, so they live with the models
 * instead of being spelled out at every call site.
 */
object CatalogExtras {
    /** Free-text search parameter. */
    const val SEARCH = "search"

    /** Pagination offset. */
    const val SKIP = "skip"

    /** Genre filter. */
    const val GENRE = "genre"

    /**
     * Cinemeta's parameter carrying the ids a `calendar-videos` catalog should
     * answer for.
     */
    const val CALENDAR_VIDEO_IDS = "calendarVideosIds"
}
