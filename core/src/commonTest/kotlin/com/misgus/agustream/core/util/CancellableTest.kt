package com.misgus.agustream.core.util

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CancellableTest {
    @Test
    fun capturesRegularFailures() {
        val result = runCatchingCancellable { error("boom") }
        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun returnsSuccessValue() {
        assertEquals(42, runCatchingCancellable { 42 }.getOrNull())
    }

    @Test
    fun rethrowsCancellation() {
        assertFailsWith<CancellationException> {
            runCatchingCancellable { throw CancellationException("cancelled") }
        }
    }
}
