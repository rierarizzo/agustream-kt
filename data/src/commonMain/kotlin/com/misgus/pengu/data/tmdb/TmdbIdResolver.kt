package com.misgus.pengu.data.tmdb

import com.misgus.pengu.core.model.ContentType
import com.misgus.pengu.core.model.ContentTypes
import com.misgus.pengu.core.model.TmdbMediaType
import com.misgus.pengu.core.model.releaseYear
import com.misgus.pengu.data.arrOrNull
import com.misgus.pengu.data.doubleOrNull
import com.misgus.pengu.data.intOrNull
import com.misgus.pengu.data.objOrNull
import com.misgus.pengu.data.stringOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlin.math.abs

/** A resolved TMDB id and the endpoint it belongs to. */
internal data class TmdbRef(
    val id: Int,
    val mediaType: TmdbMediaType,
)

/**
 * Turns any Stremio content id into a TMDB id.
 *
 * The cascade is, in order: an explicit `tmdb:` id is used as-is, an IMDb or
 * TVDB id goes through TMDB's `/find`, and anything else is searched by title
 * and year. Positive and negative results are cached for the process, so the
 * same title is not resolved twice (the home billboard enriches six titles at
 * once, and the detail screen then asks for the same ones).
 */
internal class TmdbIdResolver(
    private val client: TmdbClient,
) {
    private data class Entry(
        val ref: TmdbRef?,
    )

    private val mutex = Mutex()
    private val cache = mutableMapOf<String, Entry>()

    suspend fun resolve(
        parsed: ParsedContentId,
        type: ContentType,
        name: String,
        year: Int?,
        key: String,
        language: String,
    ): TmdbRef? {
        val cacheKey = "${parsed.kind}:${parsed.value}:${type.value}"
        mutex.withLock { cache[cacheKey] }?.let { return it.ref }
        val resolved = resolveUncached(parsed, type, name, year, key, language)
        mutex.withLock { cache[cacheKey] = Entry(resolved) }
        return resolved
    }

    private suspend fun resolveUncached(
        parsed: ParsedContentId,
        type: ContentType,
        name: String,
        year: Int?,
        key: String,
        language: String,
    ): TmdbRef? {
        val mediaType = if (ContentTypes.isSeries(type)) TmdbMediaType.SERIES else TmdbMediaType.MOVIE
        return when (parsed.kind) {
            ContentIdKind.THEMOVIEDB -> parsed.value.toIntOrNull()?.let { TmdbRef(it, mediaType) }
            ContentIdKind.IMDB -> findByExternal("imdb_id", parsed.value, mediaType, key, language)
            ContentIdKind.TVDB -> findByExternal("tvdb_id", parsed.value, mediaType, key, language)
            ContentIdKind.OTHER -> searchByTitle(name, year, mediaType, key, language)
        }
    }

    /** `/find` for an external id; TMDB answers both media types in one call. */
    private suspend fun findByExternal(
        source: String,
        value: String,
        mediaType: TmdbMediaType,
        key: String,
        language: String,
    ): TmdbRef? {
        val json =
            try {
                client.getJson(
                    path = "/find/$value",
                    key = key,
                    language = language,
                    params = mapOf("external_source" to source),
                )
            } catch (error: TmdbException) {
                if (error.status == NOT_FOUND) return null
                throw error
            }
        firstId(json, mediaType)?.let { return TmdbRef(it, mediaType) }
        // A title the addon typed as a movie but TMDB stores as a series (or the
        // other way around) still resolves: the find response carries both.
        val other = otherMediaType(mediaType)
        return firstId(json, other)?.let { TmdbRef(it, other) }
    }

    private fun firstId(
        json: JsonObject,
        mediaType: TmdbMediaType,
    ): Int? {
        val list = if (mediaType == TmdbMediaType.MOVIE) json["movie_results"] else json["tv_results"]
        return list
            .arrOrNull()
            ?.firstOrNull()
            ?.objOrNull()
            ?.get("id")
            .intOrNull()
    }

    private suspend fun searchByTitle(
        name: String,
        year: Int?,
        mediaType: TmdbMediaType,
        key: String,
        language: String,
    ): TmdbRef? {
        if (name.isBlank()) return null
        val movie = mediaType == TmdbMediaType.MOVIE
        val params =
            buildMap {
                put("query", name)
                if (year != null) put(if (movie) "year" else "first_air_date_year", year.toString())
            }
        val json = client.getJson(if (movie) "/search/movie" else "/search/tv", key, language, params)
        val results = json["results"].arrOrNull()?.mapNotNull { it.objOrNull() } ?: return null
        return pickMatch(results, name, year, mediaType)
    }

    /**
     * Picks the result that actually is the title.
     *
     * A search returns the popular titles first, which is exactly how a remake,
     * a sequel or a same-name film wins and puts the wrong artwork on a page. So
     * only a normalized exact title, or a close one whose year also lines up,
     * is accepted; otherwise the enrichment is skipped.
     */
    private fun pickMatch(
        results: List<JsonObject>,
        name: String,
        year: Int?,
        mediaType: TmdbMediaType,
    ): TmdbRef? {
        val wanted = normalizeTitle(name)
        if (wanted.isEmpty()) return null
        return results
            .mapNotNull { result ->
                val id = result["id"].intOrNull() ?: return@mapNotNull null
                val title = (result["title"] ?: result["name"]).stringOrNull() ?: return@mapNotNull null
                val candidate = normalizeTitle(title)
                val exact = candidate == wanted
                val close = candidate.startsWith(wanted) || wanted.startsWith(candidate)
                val candidateYear = releaseYear((result["release_date"] ?: result["first_air_date"]).stringOrNull())
                val sameYear = year != null && candidateYear != null && abs(candidateYear - year) <= 1
                val accepted = exact || (close && (year == null || sameYear))
                if (!accepted) return@mapNotNull null
                Scored(
                    ref = TmdbRef(id, mediaType),
                    rank = (if (exact) 2 else 0) + (if (sameYear) 1 else 0),
                    popularity = result["popularity"].doubleOrNull() ?: 0.0,
                )
            }.maxWithOrNull(compareBy({ it.rank }, { it.popularity }))
            ?.ref
    }

    private fun otherMediaType(mediaType: TmdbMediaType): TmdbMediaType =
        if (mediaType == TmdbMediaType.MOVIE) TmdbMediaType.SERIES else TmdbMediaType.MOVIE

    private data class Scored(
        val ref: TmdbRef,
        val rank: Int,
        val popularity: Double,
    )

    private companion object {
        const val NOT_FOUND = 404
    }
}

/** Lowercase, letters and digits only, so `Spider-Man: No Way Home` matches itself. */
internal fun normalizeTitle(value: String): String = value.lowercase().filter { it.isLetterOrDigit() }
