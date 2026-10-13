package com.misgus.agustream.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The sections reachable from the top navigation.
 *
 * The order here is the order shown in the bar.
 */
enum class AppSection(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    SEARCH("Search", Icons.Outlined.Search, Icons.Filled.Search),
    LIBRARY("Library", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    CALENDAR("Calendar", Icons.Outlined.CalendarToday, Icons.Filled.CalendarToday),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
}
