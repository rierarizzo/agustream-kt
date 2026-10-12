package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.AddonCatalogExtra
import com.misgus.agustream.core.model.MetaDetail
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

    /**
     * Whether the catalog can be fetched with no query parameter.
     *
     * A catalog with a required extra cannot be browsed: it needs a query its
     * grid does not have (a search term, a list of ids, ...), and asking anyway
     * just returns empty. Home and Discover only use browsable catalogs; Search
     * uses [searches].
     */
    val browsable: Boolean get() = extras.none { it.isRequired }
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

    /**
     * Detailed metas for a catalog that answers with `metasDetailed` instead of
     * `metas` — the Cinemeta convention for `calendar-videos` and `last-videos`,
     * which return the full meta (with `videos`) for the ids passed in `extra`.
     *
     * The default is empty, so a repository without such a catalog needs nothing.
     */
    suspend fun detailedItems(
        ref: CatalogRef,
        extra: Map<String, String>? = null,
    ): List<MetaDetail> = emptyList()
}
