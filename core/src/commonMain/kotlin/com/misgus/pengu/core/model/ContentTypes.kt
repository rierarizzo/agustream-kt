package com.misgus.pengu.core.model

/**
 * Stremio content type identifiers, in one place.
 *
 * The protocol uses plain strings; naming them here keeps a typo in a `when` or
 * a filter from silently changing behavior.
 */
object ContentTypes {
    const val MOVIE = "movie"
    const val SERIES = "series"

    /**
     * Types that behave like a series: they have seasons/episodes and take part
     * in the watched rules. Some addons use one of the aliases instead of
     * `series`.
     */
    val SERIES_LIKE = setOf(SERIES, "show", "tv", "tvshow", "anime")

    /** Whether [type] is a series (or one of its known aliases). */
    fun isSeries(type: String): Boolean = type.trim().lowercase() in SERIES_LIKE
}
