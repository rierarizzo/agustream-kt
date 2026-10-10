package com.misgus.agustream.data.backend

import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AccountState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** [AccountRepository] backed by a Nuvio (Supabase) deployment. */
class NuvioAccountRepository(
    private val client: NuvioClient,
) : AccountRepository {
    private val _state = MutableStateFlow(AccountState())
    override val state: StateFlow<AccountState> = _state.asStateFlow()

    override suspend fun signIn(
        email: String,
        password: String,
    ) {
        client.signIn(email, password)
        _state.value =
            AccountState(
                isSignedIn = true,
                email = client.session?.email,
                requiresProfile = true,
            )
        // Loaded here so the profile picker has something to show right away. A
        // failure is captured, never thrown: the session is valid either way.
        loadProfiles()
    }

    override suspend fun restoreSession(): Boolean {
        if (!client.restoreSession()) return false
        _state.value =
            AccountState(
                isSignedIn = true,
                email = client.session?.email,
                requiresProfile = true,
            )
        loadProfiles()
        return true
    }

    override suspend fun signOut() {
        client.signOut()
        _state.value = AccountState(isSignedIn = false, requiresProfile = true)
    }

    override suspend fun loadProfiles() {
        if (!_state.value.isSignedIn) return
        _state.update { it.copy(isLoadingProfiles = true, profilesError = null) }
        try {
            val rows = client.select("profiles", order = "profile_index.asc")
            _state.update {
                it.copy(
                    profiles = rows.map(::backendProfileFromRow),
                    isLoadingProfiles = false,
                    profilesError = null,
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            _state.update {
                it.copy(profiles = emptyList(), isLoadingProfiles = false, profilesError = error)
            }
        }
    }

    override suspend fun selectProfile(profile: BackendProfile) {
        if (profile.profileId == _state.value.activeProfile?.profileId) return
        _state.update { it.copy(activeProfile = profile) }
    }
}
