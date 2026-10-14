package com.misgus.agustream.data.backend

import com.misgus.agustream.core.BackendException
import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.core.repository.AccountRepository

/**
 * Active profile, or a [BackendException] when none is selected.
 *
 * Row-level security on the backend only stops at the account, so every profile
 * scoped read/write must resolve one first.
 */
internal fun AccountRepository.requireActiveProfile(): BackendProfile =
    state.value.activeProfile ?: throw BackendException("No profile selected")

/**
 * Integer id of the active profile, or a [BackendException] when none is
 * selected.
 */
internal fun AccountRepository.requireProfileId(): Int = requireActiveProfile().profileId ?: throw BackendException("No profile selected")
