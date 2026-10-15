package com.misgus.pengu.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgus.pengu.core.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel whose content is scoped to the signed-in account and its profile.
 *
 * It owns the one rule every data section shares: load when there is a session
 * and (when the backend requires one) a chosen profile, and clear when the user
 * signs out. A backend without profiles never waits for one.
 *
 * Subclasses only say what "ready" and "cleared" mean for their own state.
 */
abstract class AccountScopedViewModel(
    protected val account: AccountRepository,
) : ViewModel() {
    /** The account is usable: load or refresh. */
    protected abstract fun onAccountReady()

    /** There is no session (or the user signed out): drop the loaded state. */
    protected abstract fun onAccountCleared()

    init {
        // `viewModelScope` runs on `Dispatchers.Main.immediate`, which executes
        // inline when the ViewModel is built on the main thread. The subclasses'
        // state properties are assigned after this constructor, so an inline
        // first emission would call [onAccountReady] before `_state` exists and
        // crash with an NPE. `Dispatchers.Main` always reschedules, so the
        // collection starts once construction has finished.
        viewModelScope.launch(Dispatchers.Main) {
            account.state.collect { state ->
                when {
                    !state.isSignedIn -> onAccountCleared()
                    // A backend with profiles has no safe default: wait until one
                    // is chosen instead of loading (and failing) without it.
                    state.requiresProfile && state.activeProfile == null -> Unit
                    else -> onAccountReady()
                }
            }
        }
    }
}
