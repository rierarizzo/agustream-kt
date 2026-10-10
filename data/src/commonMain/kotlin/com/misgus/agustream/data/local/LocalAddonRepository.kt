package com.misgus.agustream.data.local

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.repository.AddonRepository

/**
 * [AddonRepository] for local mode: a fixed list configured at startup.
 *
 * There is no addon management UI yet, so the list comes from the composition.
 */
class LocalAddonRepository(private val addons: List<Addon>) : AddonRepository {
    override suspend fun all(): List<Addon> = addons
}
