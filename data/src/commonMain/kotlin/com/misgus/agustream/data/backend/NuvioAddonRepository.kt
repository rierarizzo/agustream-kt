package com.misgus.agustream.data.backend

import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AddonRepository

/**
 * [AddonRepository] backed by a Nuvio (Supabase) deployment.
 *
 * Addons belong to a profile, and row-level security only stops at the account,
 * so reads are scoped by `profile_id` like the library.
 */
class NuvioAddonRepository(
    private val client: NuvioClient,
    private val account: AccountRepository,
) : AddonRepository {
    override suspend fun all(): List<Addon> {
        val profileId = account.state.value.activeProfile?.profileId
            ?: throw BackendException("No profile selected")
        val rows = client.select(
            "addons",
            order = "sort_order.asc",
            filter = "profile_id=eq.$profileId&enabled=is.true",
        )
        return rows.map(::addonFromRow)
    }
}
