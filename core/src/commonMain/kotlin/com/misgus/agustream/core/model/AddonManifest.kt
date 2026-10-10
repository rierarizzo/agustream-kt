package com.misgus.agustream.core.model

/**
 * A Stremio addon manifest, fetched from `<base>/manifest.json`.
 *
 * Plain domain data: parsing lives in the data layer.
 */
data class AddonManifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val logo: String? = null,
    val background: String? = null,
    val contactEmail: String? = null,
    /** Content types the addon serves, e.g. `movie`, `series`. */
    val types: List<String> = emptyList(),
    /** Id prefixes the addon understands, e.g. `tt` for IMDb. */
    val idPrefixes: List<String> = emptyList(),
    val resources: List<AddonResource> = emptyList(),
    val catalogs: List<AddonCatalog> = emptyList(),
) {
    /** Whether the addon declares [resource] (e.g. `stream`) for [type]. */
    fun supports(resource: String, type: String): Boolean = resources.any { candidate ->
        candidate.name == resource && (candidate.types.isEmpty() || type in candidate.types)
    }
}

/**
 * One entry of `manifest.resources`.
 *
 * The protocol allows the short form (`"stream"`) or the long form
 * (`{ "name": "stream", "types": [...] }`).
 */
data class AddonResource(
    val name: String,
    val types: List<String> = emptyList(),
    val idPrefixes: List<String>? = null,
)

/** One entry of `manifest.catalogs`. */
data class AddonCatalog(
    val type: String,
    val id: String,
    val name: String,
    val extra: List<AddonCatalogExtra> = emptyList(),
    val genres: List<String> = emptyList(),
)

/** A supported `extra` parameter of a catalog, e.g. `search`, `genre`, `skip`. */
data class AddonCatalogExtra(
    val name: String,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
)
