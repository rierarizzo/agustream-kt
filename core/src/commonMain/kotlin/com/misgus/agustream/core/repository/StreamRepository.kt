package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.Stream

/** Streams for one title, grouped by the addon that produced them. */
data class StreamGroup(
    /** Display name of the addon, or its host when the addon has no name. */
    val addonName: String,
    val streams: List<Stream>,
)

/**
 * Reads playable sources from the account's addons.
 *
 * Streams are aggregated from every addon (unlike metadata, which is
 * first-wins), because each addon usually contributes different sources.
 */
interface StreamRepository {
    /**
     * Streams for [id], where [id] is a movie id or a series episode id
     * (`tt123:1:2`). [type] is `movie`, `series`, ...
     */
    suspend fun all(
        type: String,
        id: String,
    ): List<StreamGroup>
}
