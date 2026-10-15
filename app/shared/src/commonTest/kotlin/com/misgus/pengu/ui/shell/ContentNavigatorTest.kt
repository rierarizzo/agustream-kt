package com.misgus.pengu.ui.shell

import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.player.PlaybackTarget
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentNavigatorTest {
    private val item = LibraryItem(id = "tt1", contentId = "tt1", contentType = "movie", name = "Movie")

    @Test
    fun replaceKeepsTheStackDepth() {
        val navigator = ContentNavigator()
        navigator.push(ContentRoute.Detail(item))
        navigator.push(ContentRoute.Player(PlaybackTarget.raw("https://a/1.mp4")))
        navigator.replace(ContentRoute.Player(PlaybackTarget.raw("https://a/2.mp4")))

        assertEquals(2, navigator.stack.size)
        val current = navigator.current as ContentRoute.Player
        assertEquals("https://a/2.mp4", current.target.source)
    }

    @Test
    fun replaceOnAnEmptyStackPushes() {
        val navigator = ContentNavigator()
        navigator.replace(ContentRoute.Detail(item))
        assertEquals(1, navigator.stack.size)
    }
}
