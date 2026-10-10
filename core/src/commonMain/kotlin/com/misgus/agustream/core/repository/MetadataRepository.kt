package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaPreview

/**
 * Reads title metadata from Stremio addons.
 *
 * Metadata comes from the account's enabled addons, in their order: the first
 * one that answers wins.
 */
interface MetadataRepository {
    /** Full metadata for [id] in [type], or `null` when no addon has it. */
    suspend fun detail(
        type: String,
        id: String,
    ): MetaDetail?

    /**
     * Titles similar to [id], used by the "More like this" row.
     *
     * Best-effort: picks a catalog from an addon manifest and queries it by
     * [genre]. Returns an empty list when no addon can do it.
     */
    suspend fun similar(
        type: String,
        id: String,
        genre: String? = null,
    ): List<MetaPreview>
}
