package com.misgus.pengu.data.local

import com.misgus.pengu.core.model.BackendProfile
import com.misgus.pengu.core.repository.AccountRepository
import com.misgus.pengu.core.repository.AccountState
import com.misgus.pengu.core.repository.SignInMethod
import com.misgus.pengu.core.repository.SignInRequest
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

    /** Local mode is always signed in, so it offers no sign-in method. */
    override val signInMethods: List<SignInMethod> = emptyList()

    override suspend fun signIn(request: SignInRequest) = Unit

    override suspend fun restoreSession(): Boolean = true

    override suspend fun signOut() = Unit

    override suspend fun loadProfiles() = Unit

    override suspend fun selectProfile(profile: BackendProfile) = Unit
}
