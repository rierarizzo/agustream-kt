package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.AddonCatalogExtra
import com.misgus.agustream.core.model.MetaPreview

/**
 * One catalog offered by an addon, ready to be fetched.
 *
 * @property name Display name of the catalog.
 */
data class CatalogRef(
    /** Display name of the addon that owns the catalog. */
    val addonName: String,
    /** Addon base URL (no `/manifest.json`). */
    val addonBaseUrl: String,
    /** `movie`, `series`, ... */
    val type: String,
    /** Catalog id inside the addon manifest. */
    val id: String,
    val name: String,
    /**
     * `extra` parameters the catalog declares (e.g. `search`, `genre`, `skip`).
     *
     * The UI reads it to know which catalogs accept a search query without
     * fetching every manifest itself.
     */
    val extras: List<AddonCatalogExtra> = emptyList(),
) {
    /** Stable key for dedupe and identity. */
    val key: String get() = "$addonBaseUrl|$type|$id"

    /** Whether the catalog accepts a free-text `search` parameter. */
    val searches: Boolean get() = extras.any { it.name == "search" }
}

/**
 * Reads the catalogs the account's addons expose, and their items.
 *
 * The UI depends on this interface, never on the addon client in `data/`.
 */
interface CatalogRepository {
    /** Catalogs offered by the enabled addons, in the account's order. */
    suspend fun catalogs(): List<CatalogRef>

    /** Items of [ref]. [extra] holds protocol params such as `genre` or `search`. */
    suspend fun items(
        ref: CatalogRef,
        extra: Map<String, String>? = null,
    ): List<MetaPreview>
}
