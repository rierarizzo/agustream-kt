package com.misgus.agustream.ui.components

import com.misgus.agustream.core.BackendException

/** Shown when the failure has no cause the user can act on. */
private const val GENERIC_ERROR = "Something went wrong. Check your connection and try again."

/**
 * Turns a failure into a sentence that is safe and useful to show in the UI.
 *
 * Exceptions carry developer detail (a transport error, a JSON parse failure, a
 * stack trace); that detail belongs in the logs, not on screen. Only causes the
 * user can act on are surfaced, and everything else collapses to
 * [GENERIC_ERROR] instead of leaking `error.toString()`.
 */
fun Throwable.toUserMessage(): String {
    val backend = this as? BackendException ?: return GENERIC_ERROR
    val status = backend.statusCode
    return when {
        status == 401 || status == 403 -> "Your session has expired. Please sign in again."
        status == 404 -> "The requested content could not be found."
        status != null && status >= 500 -> "The server reported a problem. Please try again in a moment."
        else -> GENERIC_ERROR
    }
}
