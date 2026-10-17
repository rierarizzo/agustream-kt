package com.misgus.pengu.data.tmdb

/** Which external provider a Stremio content id names. */
internal enum class ContentIdKind {
    IMDB,
    THEMOVIEDB,
    TVDB,

    /** Any other addon id (`kitsu:...`, a bare number, ...), resolved by title. */
    OTHER,
}

/**
 * A content id split into the provider and the value that provider expects.
 *
 * The episode suffix Stremio appends (`tt0133093:1:2`) is stripped, so a base id
 * is resolved even when it arrives as an episode. [raw] keeps the original, for
 * logs and for the title fallback.
 */
internal data class ParsedContentId(
    val kind: ContentIdKind,
    val value: String,
)

private val IMDB_ID = Regex("^(?:tt|imdb)[:-]?(\\d+)(?::\\d+:\\d+)?$", RegexOption.IGNORE_CASE)
private val TMDB_ID = Regex("^(?:tmdb|themoviedb)[-:](\\d+)(?::\\d+:\\d+)?$", RegexOption.IGNORE_CASE)
private val TVDB_ID = Regex("^(?:tvdb|thetvdb)[-:](\\d+)(?::\\d+:\\d+)?$", RegexOption.IGNORE_CASE)

/**
 * Parses [id] into the shape TMDB needs.
 *
 * A bare number is deliberately `OTHER`, not a TMDB id: Kitsu and other addons
 * use raw numbers too, so treating one as a TMDB id would resolve the wrong
 * title. An explicit `tmdb:` prefix is the only way a numeric id is trusted.
 */
internal fun parseContentId(id: String): ParsedContentId {
    val raw = id.trim()
    IMDB_ID.matchEntire(raw)?.let { return ParsedContentId(ContentIdKind.IMDB, "tt${it.groupValues[1]}") }
    TMDB_ID.matchEntire(raw)?.let { return ParsedContentId(ContentIdKind.THEMOVIEDB, it.groupValues[1]) }
    TVDB_ID.matchEntire(raw)?.let { return ParsedContentId(ContentIdKind.TVDB, it.groupValues[1]) }
    return ParsedContentId(ContentIdKind.OTHER, raw)
}
