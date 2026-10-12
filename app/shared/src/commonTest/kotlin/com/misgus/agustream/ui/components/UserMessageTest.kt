package com.misgus.agustream.ui.components

import com.misgus.agustream.core.BackendException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UserMessageTest {
    @Test
    fun `expired session has a dedicated message`() {
        assertEquals(
            "Your session has expired. Please sign in again.",
            BackendException("Unauthorized", statusCode = 401).toUserMessage(),
        )
    }

    @Test
    fun `server failure has a dedicated message`() {
        assertEquals(
            "The server reported a problem. Please try again in a moment.",
            BackendException("Boom", statusCode = 503).toUserMessage(),
        )
    }

    @Test
    fun `technical detail is not leaked`() {
        val message =
            IllegalStateException(
                "io.ktor.client.network.ConnectException: connection refused",
            ).toUserMessage()
        assertFalse(message.contains("Exception"))
        assertEquals("Something went wrong. Check your connection and try again.", message)
    }
}
