package com.misgus.agustream.data.backend

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AddonRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [AddonRepository] backed by a Nuvio (Supabase) deployment.
 *
 * Addons belong to a profile, and row-level security only stops at the account,
 * so reads are scoped by `profile_id` like the library.
 *
 * The list is cached per profile: every metadata, stream and subtitle read asks
 * for it, and the watched-badge sync resolves many series at once. The cache is
 * keyed by the profile's uuid (globally unique, unlike the integer
 * `profile_id`), so switching profile or account refreshes it, and addons have
 * no write path in this repository, so a cached list cannot go stale.
 */
class NuvioAddonRepository(
    private val rest: NuvioRest,
    private val account: AccountRepository,
) : AddonRepository {
    private val cacheMutex = Mutex()
    private var cachedProfileKey: String? = null
    private var cachedAddons: List<Addon>? = null

    override suspend fun all(): List<Addon> {
        val profile = account.requireActiveProfile()
        val profileId = account.requireProfileId()
        val profileKey = profile.id
        cachedAddons?.takeIf { cachedProfileKey == profileKey }?.let { return it }
        // Serialize the fetch so concurrent callers share one request.
        return cacheMutex.withLock {
            cachedAddons?.takeIf { cachedProfileKey == profileKey }?.let { return@withLock it }
            val rows =
                rest.select(
                    "addons",
                    order = "sort_order.asc",
                    filter = "profile_id=eq.$profileId&enabled=is.true",
                )
            val addons = rows.map(::addonFromRow)
            cachedProfileKey = profileKey
            cachedAddons = addons
            addons
        }
    }
}
