package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.Addon

/**
 * Reads the addons installed in the account.
 *
 * Metadata for a title is not tied to one addon: like Nuvio does, it is looked
 * up across the enabled addons, so the UI needs the list to know where to ask.
 */
interface AddonRepository {
    /** Enabled addons of the active profile, in the user's order. */
    suspend fun all(): List<Addon>
}
