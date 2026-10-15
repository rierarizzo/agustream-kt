package com.misgus.pengu.ui.components

import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.error_generic
import com.misgus.pengu.app.shared.generated.resources.error_server
import com.misgus.pengu.app.shared.generated.resources.error_session_expired
import com.misgus.pengu.core.BackendException
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
