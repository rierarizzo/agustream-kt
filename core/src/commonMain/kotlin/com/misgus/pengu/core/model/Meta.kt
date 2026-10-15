package com.misgus.pengu.core.model

/** A catalog item, called a "meta preview" in Stremio terms. */
data class MetaPreview(
    val id: String,
    val type: String,
    val name: String,
    val poster: String? = null,
    /** `poster` or `landscape`. */
    val posterShape: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: Double? = null,
    val genres: List<String> = emptyList(),
)

/**
 * A full metadata record, called a "meta detail" in Stremio terms.
 *
 * Credits are merged from `app_extras`, the top-level (deprecated) fields and
 * `links` in the data layer.
 */
data class MetaDetail(
    val id: String,
    val type: String,
    val name: String,
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: Double? = null,
    val runtime: String? = null,
    val genres: List<String> = emptyList(),
    /** Episodes for series, or a single entry for movies. */
    val videos: List<MetaVideo> = emptyList(),
    /** Actors, with the character they play. */
    val cast: List<MetaPerson> = emptyList(),
    val director: List<String> = emptyList(),
    val writer: List<String> = emptyList(),
    val released: String? = null,
    val country: String? = null,
)

/** One video of a [MetaDetail] (an episode, or the movie itself). */
data class MetaVideo(
    val id: String,
    val title: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val number: Int? = null,
    val released: String? = null,
    val thumbnail: String? = null,
)

/** One person of a [MetaDetail]'s cast or crew. */
data class MetaPerson(
    val name: String,
    /** Character played (cast) or role held (crew). */
    val character: String? = null,
    /** Portrait URL. */
    val photo: String? = null,
)
