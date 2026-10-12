package com.misgus.agustream.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailSectionsTest {
    @Test
    fun `formats a plain date without leading zeros`() {
        assertEquals("1 March 2024", formatReleaseDate("2024-03-01"))
    }

    @Test
    fun `formats a full ISO datetime using only the date`() {
        assertEquals("1 March 2024", formatReleaseDate("2024-03-01T00:00:00.000Z"))
        assertEquals("25 December 2023", formatReleaseDate("2023-12-25T20:00:00Z"))
    }

    @Test
    fun `returns null when there is no usable date`() {
        assertNull(formatReleaseDate(null))
        assertNull(formatReleaseDate(""))
        assertNull(formatReleaseDate("coming soon"))
    }

    @Test
    fun `returns null for an out-of-range month`() {
        assertNull(formatReleaseDate("2024-13-01"))
    }
}
