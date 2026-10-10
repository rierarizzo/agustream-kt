package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.Subtitle

/** Reads subtitle tracks from the account's addons. */
interface SubtitleRepository {
    /**
     * Subtitle tracks for [id] (`tt123` or `tt123:1:2`), where [type] is
     * `movie`, `series`, ...
     */
    suspend fun all(
        type: String,
        id: String,
    ): List<Subtitle>
}

/** Repository that offers no subtitles (used when there is no addon layer). */
class NoSubtitleRepository : SubtitleRepository {
    override suspend fun all(
        type: String,
        id: String,
    ): List<Subtitle> = emptyList()
}
