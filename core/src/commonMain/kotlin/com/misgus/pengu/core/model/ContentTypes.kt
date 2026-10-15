package com.misgus.pengu.core.model

/**
 * A Stremio content type (`movie`, `series`, ...).
 *
 * A value class over the protocol string, so a content type cannot be passed
 * where a different `String` is expected (or the other way around) without the
 * compiler noticing. [value] is the protocol token, used only at the HTTP and
 * JSON boundaries.
 */
@JvmInline
value class ContentType(
    val value: String,
) {
    override fun toString(): String = value
}

/**
 * Stremio content type identifiers, in one place.
 *
 * The protocol uses plain strings; naming them here keeps a typo in a `when` or
 * a filter from silently changing behavior. The typed [ContentType] is the
 * domain counterpart, and the constants below are already typed so call sites
 * compare like with like.
 */
object ContentTypes {
    val MOVIE = ContentType("movie")
    val SERIES = ContentType("series")

    /**
     * Types that behave like a series: they have seasons/episodes and take part
     * in the watched rules. Some addons use one of the aliases instead of
     * `series`.
     */
    private val SERIES_LIKE = setOf(SERIES.value, "show", "tv", "tvshow", "anime")

    /** Whether [type] is a series (or one of its known aliases). */
    fun isSeries(type: ContentType): Boolean = type.value.trim().lowercase() in SERIES_LIKE
}
