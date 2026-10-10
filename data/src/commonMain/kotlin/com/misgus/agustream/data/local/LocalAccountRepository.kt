package com.misgus.agustream.data.local

import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AccountState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [AccountRepository] for local mode: there is always a session and there are
 * no profiles, because the data lives on this machine.
 *
 * `requiresProfile` is `false`, so the app never shows the profile picker.
 */
class LocalAccountRepository : AccountRepository {
    private val _state =
        MutableStateFlow(
            AccountState(isSignedIn = true, requiresProfile = false),
        )
    override val state: StateFlow<AccountState> = _state.asStateFlow()

    override suspend fun signIn(
        email: String,
        password: String,
    ) = Unit

    override suspend fun restoreSession(): Boolean = true

    override suspend fun signOut() = Unit

    override suspend fun loadProfiles() = Unit

    override suspend fun selectProfile(profile: BackendProfile) = Unit
}
