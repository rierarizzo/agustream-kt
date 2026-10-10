package com.misgus.agustream.ui.shell

import androidx.compose.runtime.mutableStateListOf
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.core.repository.CatalogRef

/** A pushed content route (detail, full catalog or the player). */
sealed interface ContentRoute {
    data class Detail(
        val item: LibraryItem,
    ) : ContentRoute

    data class Catalog(
        val ref: CatalogRef,
    ) : ContentRoute

    data class Player(
        val target: PlaybackTarget,
    ) : ContentRoute
}

/**
 * Stack of routes shown inside the shell's content area.
 *
 * The rail and title bar stay visible while these are pushed, and dialogs still
 * cover the whole window because they are hosted at the root.
 */
class ContentNavigator {
    val stack = mutableStateListOf<ContentRoute>()

    val current: ContentRoute? get() = stack.lastOrNull()
    val canGoBack: Boolean get() = stack.isNotEmpty()

    fun push(route: ContentRoute) {
        stack.add(route)
    }

    fun pop() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    /**
     * Replaces the top route without growing the stack.
     *
     * Used by the player's next-episode autoplay so "back" still leaves the
     * player instead of walking through every episode that auto-played.
     */
    fun replace(route: ContentRoute) {
        if (stack.isEmpty()) stack.add(route) else stack[stack.lastIndex] = route
    }

    fun clear() {
        stack.clear()
    }
}
