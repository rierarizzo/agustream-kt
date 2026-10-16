package com.misgus.pengu.ui.shell

import androidx.compose.ui.graphics.vector.ImageVector
import com.misgus.pengu.ui.icons.AppIcons

/**
 * The sections reachable from the navigation.
 *
 * The order here is the order shown in the bar.
 */
enum class AppSection(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("Home", AppIcons.Outlined.Home, AppIcons.Filled.Home),
    SEARCH("Search", AppIcons.Outlined.Search, AppIcons.Filled.Search),
    LIBRARY("Library", AppIcons.Outlined.BookmarkBorder, AppIcons.Filled.Bookmark),
    CALENDAR("Calendar", AppIcons.Outlined.CalendarToday, AppIcons.Filled.CalendarToday),
    SETTINGS("Settings", AppIcons.Outlined.Settings, AppIcons.Filled.Settings),
}
