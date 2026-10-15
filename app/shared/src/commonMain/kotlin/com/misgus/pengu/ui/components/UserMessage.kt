package com.misgus.pengu.ui.components

import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.error_generic
import com.misgus.pengu.app.shared.generated.resources.error_not_found
import com.misgus.pengu.app.shared.generated.resources.error_server
import com.misgus.pengu.app.shared.generated.resources.error_session_expired
import com.misgus.pengu.core.BackendException
import org.jetbrains.compose.resources.StringResource

/**
 * Turns a failure into a message that is safe and useful to show in the UI.
 *
 * Exceptions carry developer detail (a transport error, a JSON parse failure, a
 * stack trace); that detail belongs in the logs, not on screen. Only causes the
 * user can act on are surfaced, and everything else collapses to
 * [Res.string.error_generic] instead of leaking `error.toString()`.
 *
 * Returns the resource rather than the text so the caller resolves it with
 * `stringResource` and the message stays translated and testable.
 */
fun Throwable.toUserMessage(): StringResource {
    val backend = this as? BackendException ?: return Res.string.error_generic
    val status = backend.statusCode
    return when {
        status == 401 || status == 403 -> Res.string.error_session_expired
        status == 404 -> Res.string.error_not_found
        status != null && status >= 500 -> Res.string.error_server
        else -> Res.string.error_generic
    }
}
