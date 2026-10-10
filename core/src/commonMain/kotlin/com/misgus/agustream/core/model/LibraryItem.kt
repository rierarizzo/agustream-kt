package com.misgus.agustream.core.model

import kotlin.time.Instant

/** One saved title in the library. */
data class LibraryItem(
    val id: String,
    /** Addon content id, e.g. `tt1254207`. */
    val contentId: String,
    /** `movie`, `series`, ... */
    val contentType: String,
    val name: String,
    val poster: String? = null,
    val posterShape: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: Double? = null,
    val genres: List<String> = emptyList(),
    /** Base URL of the addon this item came from. */
    val addonBaseUrl: String? = null,
    /** When the item was added. */
    val addedAt: Instant? = null,
    val profileId: Int? = null,
) {
    /** Release year, parsed from [releaseInfo] (`2019`, `2019-2023`, ...). */
    val year: Int?
        get() = YEAR_PATTERN.find(releaseInfo ?: "")?.value?.toIntOrNull()

    companion object {
        private val YEAR_PATTERN = Regex("(?:1[89]\\d\\d|20\\d\\d)")

        /**
         * Builds a library item from a catalog preview, so a title found outside
         * the library opens the same detail screen.
         */
        fun fromPreview(preview: MetaPreview): LibraryItem = LibraryItem(
            id = preview.id,
            contentId = preview.id,
            contentType = preview.type,
            name = preview.name,
            poster = preview.poster,
            posterShape = preview.posterShape,
            background = preview.background,
            logo = preview.logo,
            description = preview.description,
            releaseInfo = preview.releaseInfo,
            imdbRating = preview.imdbRating,
            genres = preview.genres,
        )
    }
}
