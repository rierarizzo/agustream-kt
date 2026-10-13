package com.misgus.agustream.ui.components

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.error_generic
import agustream.app.shared.generated.resources.error_server
import agustream.app.shared.generated.resources.error_session_expired
import com.misgus.agustream.core.BackendException
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMessageTest {
    @Test
    fun `expired session has a dedicated message`() {
        assertEquals(
            Res.string.error_session_expired,
            BackendException("Unauthorized", statusCode = 401).toUserMessage(),
        )
    }

    @Test
    fun `server failure has a dedicated message`() {
        assertEquals(
            Res.string.error_server,
            BackendException("Boom", statusCode = 503).toUserMessage(),
        )
    }

    @Test
    fun `technical detail is not leaked`() {
        val message =
            IllegalStateException(
                "io.ktor.client.network.ConnectException: connection refused",
            ).toUserMessage()
        assertEquals(Res.string.error_generic, message)
    }
}
