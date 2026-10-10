package com.misgus.agustream.data.backend

import kotlin.test.Test
import kotlin.test.assertEquals

class PostgrestFiltersTest {
    @Test
    fun escapesReservedCharactersAndPercentEncodesThem() {
        // `:` is structural in PostgREST, so it is backslash-escaped (`%5C`)
        // before the whole value is percent-encoded (`%3A`).
        assertEquals("tt1%5C%3A2%5C%3A3", postgrestLiteral("tt1:2:3"))
        assertEquals("a%5C%2Cb", postgrestLiteral("a,b"))
        assertEquals("%5C%28x%5C%29", postgrestLiteral("(x)"))
        assertEquals("a%5C.b", postgrestLiteral("a.b"))
    }

    @Test
    fun encodesSpacesAndKeepsUnreservedCharacters() {
        assertEquals("Batman%20Begins", postgrestLiteral("Batman Begins"))
        assertEquals("tt1234", postgrestLiteral("tt1234"))
    }

    @Test
    fun escapesBackslashes() {
        assertEquals("a%5C%5Cb", postgrestLiteral("a\\b"))
    }
}
