package com.misgus.pengu.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentBoundsTest {
    // The scan is index-based: a test turns the linear index back into (x, y).
    private fun boundsOf(
        width: Int,
        height: Int,
        content: (x: Int, y: Int) -> Boolean,
    ): ContentBounds = contentBounds(width, height) { index -> content(index % width, index / width) }

    @Test
    fun `a fully transparent image has no content`() {
        assertTrue(boundsOf(width = 4, height = 3) { _, _ -> false }.isEmpty)
    }

    @Test
    fun `the content rectangle is reported`() {
        val bounds = boundsOf(width = 5, height = 4) { x, y -> x in 1..3 && y in 1..2 }

        assertEquals(1, bounds.left)
        assertEquals(1, bounds.top)
        assertEquals(3, bounds.right)
        assertEquals(2, bounds.bottom)
        assertEquals(3, bounds.width)
        assertEquals(2, bounds.height)
    }

    @Test
    fun `a single pixel is a one-by-one box`() {
        val bounds = boundsOf(width = 5, height = 4) { x, y -> x == 2 && y == 3 }

        assertEquals(2, bounds.left)
        assertEquals(3, bounds.top)
        assertEquals(2, bounds.right)
        assertEquals(3, bounds.bottom)
        assertEquals(1, bounds.width)
        assertEquals(1, bounds.height)
    }

    @Test
    fun `content reaching every edge spans the image`() {
        val bounds = boundsOf(width = 3, height = 2) { _, _ -> true }

        assertEquals(0, bounds.left)
        assertEquals(0, bounds.top)
        assertEquals(2, bounds.right)
        assertEquals(1, bounds.bottom)
        assertEquals(3, bounds.width)
        assertEquals(2, bounds.height)
    }
}
