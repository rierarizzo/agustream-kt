package com.misgus.agustream.core.repository

import com.misgus.agustream.core.model.BackendProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Immutable snapshot of the account state.
 *
 * Observable through [AccountRepository.state], so screens react instead of
 * polling.
 */
data class AccountState(
    /** Whether there is a signed-in session. */
    val isSignedIn: Boolean = false,
    /** Email of the signed-in user, when the backend reports one. */
    val email: String? = null,
    /**
     * Whether content reads are scoped to a profile that must be chosen first.
     *
     * Backends without profiles return `false`: the app is usable right after
     * sign-in and never shows a profile picker.
     */
    val requiresProfile: Boolean = true,
    /** Profiles of the signed-in account. Empty when the backend has none. */
    val profiles: List<BackendProfile> = emptyList(),
    /** `true` while [AccountRepository.loadProfiles] is in flight. */
    val isLoadingProfiles: Boolean = false,
    /** Failure of the last profile load, or `null`. */
    val profilesError: Throwable? = null,
    /**
     * Profile that library, progress and every write are scoped to.
     *
     * `null` means "not chosen yet". When [requiresProfile] is `true` it has
     * **no default**: the app asks for a profile instead of guessing, because a
     * write saved to the wrong profile would be wrong data, not merely missing.
     */
    val activeProfile: BackendProfile? = null,
)

/**
 * Account-level access: the session and the profiles behind it.
 *
 * Session tokens are deliberately **not** part of this contract: they are
 * transport details of whichever backend is in use. The UI only needs to know
 * whether there is a session and, optionally, an email.
 */
interface AccountRepository {
    val state: StateFlow<AccountState>

    /**
     * Signs in with email + password and loads the profiles.
     *
     * A profile load failure does not undo the session: it is reported through
     * [AccountState.profilesError] so the UI can offer a retry.
     */
    suspend fun signIn(email: String, password: String)

    /**
     * Restores a previously persisted session, if there is one.
     *
     * Returns `true` when a usable session is available; `false` means the
     * caller should ask for a sign-in.
     */
    suspend fun restoreSession(): Boolean

    /** Drops the session, invalidating it on the backend when possible. */
    suspend fun signOut()

    /** (Re)loads [AccountState.profiles] for the signed-in account. */
    suspend fun loadProfiles()

    /** Chooses the profile to work with. */
    suspend fun selectProfile(profile: BackendProfile)
}
