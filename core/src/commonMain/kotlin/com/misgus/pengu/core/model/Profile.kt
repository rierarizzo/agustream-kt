package com.misgus.pengu.core.model

/** One addon installed in the account. */
data class Addon(
    val id: String,
    /** Manifest URL, e.g. `https://addon.example/manifest.json`. */
    val url: String,
    val name: String? = null,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
    val profileId: Int? = null,
)

/** A profile of an account (one account can have several). */
data class BackendProfile(
    /** Primary key (uuid). */
    val id: String,
    val name: String,
    /** Integer id that `library_items` and `watch_progress` reference. */
    val profileId: Int? = null,
    val profileIndex: Int? = null,
    val avatarId: String? = null,
    val avatarUrl: String? = null,
    val avatarColorHex: String? = null,
    val pinEnabled: Boolean = false,
)
