package com.misgus.agustream.data.backend

import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AccountState
import com.misgus.agustream.core.repository.SignInMethod
import com.misgus.agustream.core.repository.SignInRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** [AccountRepository] backed by a Nuvio (Supabase) deployment. */
class NuvioAccountRepository(
    private val client: NuvioClient,
    /**
     * Scope that owns the profile load triggered by sign-in and restore.
     *
     * The load must outlive its caller: the caller is often a composable that
     * leaves the composition as soon as `isSignedIn` flips the profile gate, so
     * a load launched on that scope would be cancelled before it finishes and
     * leave the picker spinning forever.
     */
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AccountRepository {
    private val _state = MutableStateFlow(AccountState())
    override val state: StateFlow<AccountState> = _state.asStateFlow()

    override val signInMethods: List<SignInMethod> = listOf(SignInMethod.EmailPassword)

    /** In-flight profile load, so sign-out can drop it and a retry can replace it. */
    private var profileLoad: Job? = null

    override suspend fun signIn(request: SignInRequest) {
        // Exhaustive over the sealed request: a new method forces this impl to
        // decide whether it supports it.
        val credentials =
            when (request) {
                is SignInRequest.EmailPassword -> request
            }
        client.signIn(credentials.email, credentials.password)
        _state.value =
            AccountState(
                isSignedIn = true,
                email = client.session?.email,
                requiresProfile = true,
                // Set before launching so the gate never flashes "no profiles".
                isLoadingProfiles = true,
            )
        // Loaded here so the profile picker has something to show right away. A
        // failure is captured, never thrown: the session is valid either way.
        startProfileLoad()
    }

    override suspend fun restoreSession(): Boolean {
        if (!client.restoreSession()) return false
        _state.value =
            AccountState(
                isSignedIn = true,
                email = client.session?.email,
                requiresProfile = true,
                isLoadingProfiles = true,
            )
        startProfileLoad()
        return true
    }

    override suspend fun signOut() {
        profileLoad?.cancel()
        profileLoad = null
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
            // A cancelled load (sign-out or shutdown) must not leave the picker
            // spinning forever.
            _state.update { it.copy(isLoadingProfiles = false) }
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

    private fun startProfileLoad() {
        profileLoad?.cancel()
        profileLoad = scope.launch { loadProfiles() }
    }
}
