package com.misgus.agustream.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Shell state that outlives the composition: the selected section and the route
 * stack.
 *
 * Scoped to the root owner, it survives an Android configuration change (a
 * rotation) instead of resetting the shell to Home. Process death still starts
 * fresh: the stack holds rich models (`LibraryItem`, `CatalogRef`,
 * `PlaybackTarget`) that are not worth serialising just for this.
 */
class ShellViewModel : ViewModel() {
    var selected by mutableStateOf(AppSection.HOME)
        private set

    val navigator = ContentNavigator()

    /** Switches section and drops any pushed route, so a section never leaks one. */
    fun select(section: AppSection) {
        selected = section
        navigator.clear()
    }
}
