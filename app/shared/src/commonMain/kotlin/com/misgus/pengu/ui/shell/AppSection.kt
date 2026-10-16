package com.misgus.pengu.ui.shell

import androidx.compose.ui.graphics.vector.ImageVector
import com.misgus.pengu.ui.icons.Boxicons

/**
 * The sections reachable from the navigation.
 *
 * The order here is the order shown in the rail and the bottom bar. The glyphs
 * are the Boxicons set (`Boxicons`); selection is the tint alone.
 */
enum class AppSection(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Boxicons.Home),
    SEARCH("Search", Boxicons.Search),
    LIBRARY("Library", Boxicons.Bookmark),
    CALENDAR("Calendar", Boxicons.Calendar),
    SETTINGS("Settings", Boxicons.Settings),
}
