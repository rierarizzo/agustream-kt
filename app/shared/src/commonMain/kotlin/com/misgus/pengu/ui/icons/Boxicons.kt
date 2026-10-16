package com.misgus.pengu.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Icon set vendored from Boxicons (MIT).
 *
 * The whole UI uses these solid glyphs, on Boxicons' 24dp viewport; call sites
 * size them with [com.misgus.pengu.ui.theme.AppIconSize]. `scripts/gen_boxicons.mjs`
 * regenerates the file from the Boxicons set on Iconify. Only the glyphs the UI
 * uses are kept.
 */
object Boxicons {
    val Home: ImageVector by lazy { boxicon("Home", HOME) }
    val Search: ImageVector by lazy { boxicon("Search", SEARCH) }
    val Bookmark: ImageVector by lazy { boxicon("Bookmark", BOOKMARK) }
    val Calendar: ImageVector by lazy { boxicon("Calendar", CALENDAR) }
    val Settings: ImageVector by lazy { boxicon("Settings", SETTINGS) }
    val Play: ImageVector by lazy { boxicon("Play", PLAY) }
    val Plus: ImageVector by lazy { boxicon("Plus", PLUS) }
    val Check: ImageVector by lazy { boxicon("Check", CHECK) }
    val CheckCircle: ImageVector by lazy { boxicon("CheckCircle", CHECK_CIRCLE) }
    val CheckCircleOutline: ImageVector by lazy { boxicon("CheckCircleOutline", CHECK_CIRCLE_OUTLINE) }
    val Star: ImageVector by lazy { boxicon("Star", STAR) }
    val StarBorder: ImageVector by lazy { boxicon("StarBorder", STAR_BORDER) }
    val ArrowBack: ImageVector by lazy { boxicon("ArrowBack", ARROW_BACK, autoMirror = true) }
    val ChevronLeft: ImageVector by lazy { boxicon("ChevronLeft", CHEVRON_LEFT) }
    val ChevronRight: ImageVector by lazy { boxicon("ChevronRight", CHEVRON_RIGHT) }
    val ChevronDown: ImageVector by lazy { boxicon("ChevronDown", CHEVRON_DOWN) }
    val Close: ImageVector by lazy { boxicon("Close", CLOSE) }
    val Pause: ImageVector by lazy { boxicon("Pause", PAUSE) }
    val Refresh: ImageVector by lazy { boxicon("Refresh", REFRESH) }
    val Forward: ImageVector by lazy { boxicon("Forward", FORWARD) }
    val Replay: ImageVector by lazy { boxicon("Replay", REPLAY) }
    val Fullscreen: ImageVector by lazy { boxicon("Fullscreen", FULLSCREEN) }
    val Volume: ImageVector by lazy { boxicon("Volume", VOLUME) }
    val Visibility: ImageVector by lazy { boxicon("Visibility", VISIBILITY) }
    val VisibilityOff: ImageVector by lazy { boxicon("VisibilityOff", VISIBILITY_OFF) }
    val Subtitles: ImageVector by lazy { boxicon("Subtitles", SUBTITLES) }
    val Audiotrack: ImageVector by lazy { boxicon("Audiotrack", AUDIOTRACK) }
    val Info: ImageVector by lazy { boxicon("Info", INFO) }
    val Error: ImageVector by lazy { boxicon("Error", ERROR) }
    val Compass: ImageVector by lazy { boxicon("Compass", COMPASS) }
    val FolderOpen: ImageVector by lazy { boxicon("FolderOpen", FOLDER_OPEN) }
    val Inbox: ImageVector by lazy { boxicon("Inbox", INBOX) }
    val Movie: ImageVector by lazy { boxicon("Movie", MOVIE) }
    val User: ImageVector by lazy { boxicon("User", USER) }
    val SearchOff: ImageVector by lazy { boxicon("SearchOff", SEARCH_OFF) }
    val CalendarMonth: ImageVector by lazy { boxicon("CalendarMonth", CALENDAR_MONTH) }
    val Copy: ImageVector by lazy { boxicon("Copy", COPY) }
    val Login: ImageVector by lazy { boxicon("Login", LOGIN, autoMirror = true) }
    val Logout: ImageVector by lazy { boxicon("Logout", LOGOUT, autoMirror = true) }
}

/**
 * Builds one [ImageVector] from its Boxicons path, on the 24dp viewport.
 *
 * `autoMirror` flips the glyph in RTL, which is what Material's auto-mirrored
 * variants did for the back arrow and the sign-in/out icons.
 */
private fun boxicon(
    name: String,
    pathData: String,
    autoMirror: Boolean = false,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
        }.build()

private const val HOME =
    "M3 13h1v7c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-7h1c.4 0 .77-.24.92-.62c.16-.37.07-.8-.22-1.09l-8.99-9a.996.996 0 0" +
        " 0-1.41 0l-9.01 9c-.29.29-.37.72-.22 1.09s.52.62.92.62Z"
private const val SEARCH =
    "M10.5 19c1.98 0 3.81-.69 5.25-1.83L20 21.42l1.41-1.41l-4.25-4.25a8.47 8.47 0 0 0 1.83-5.25c0-4.69-3.81-8.5-8.5" +
        "-8.5S2 5.81 2 10.5S5.81 19 10.5 19m0-15c3.58 0 6.5 2.92 6.5 6.5S14.08 17 10.5 17S4 14.08 4 10.5S6.92 4 10.5 4"
private const val BOOKMARK =
    "M18 2H6c-1.1 0-2 .9-2 2v17c0 .36.19.69.5.87s.69.18 1 0l6.5-3.72l6.5 3.72c.15.09.33.13.5.13s.35-.04.5-.13c.31-." +
        "18.5-.51.5-.87V4c0-1.1-.9-2-2-2"
private const val CALENDAR =
    "M19 4h-2V2h-2v2H9V2H7v2H5c-1.1 0-2 .9-2 2v1h18V6c0-1.1-.9-2-2-2M3 20c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8H3zm12-8" +
        "h2v2h-2zm0 4h2v2h-2zm-4-4h2v2h-2zm0 4h2v2h-2zm-4-4h2v2H7zm0 4h2v2H7z"
private const val SETTINGS =
    "m21.16 7.86l-1-1.73a1.997 1.997 0 0 0-2.73-.73l-.53.31c-.58-.46-1.22-.83-1.9-1.11V4c0-1.1-.9-2-2-2h-2c-1.1 0-2" +
        " .9-2 2v.6c-.67.28-1.31.66-1.9 1.11l-.53-.31c-.96-.55-2.18-.22-2.73.73l-1 1.73c-.55.96-.22 2.18.73 2.73l.5.29c" +
        "-.05.37-.08.74-.08 1.11s.03.74.08 1.11l-.5.29c-.96.55-1.28 1.78-.73 2.73l1 1.73c.55.95 1.78 1.28 2.73.73l.53-." +
        "31c.58.46 1.22.83 1.9 1.11v.6c0 1.1.9 2 2 2h2c1.1 0 2-.9 2-2v-.6a8.7 8.7 0 0 0 1.9-1.11l.53.31c.96.55 2.18.22 " +
        "2.73-.73l1-1.73c.55-.96.22-2.18-.73-2.73l-.5-.29c.05-.37.08-.74.08-1.11s-.03-.74-.08-1.11l.5-.29c.96-.55 1.28-" +
        "1.78.73-2.73M12 16c-2.21 0-4-1.79-4-4s1.79-4 4-4s4 1.79 4 4s-1.79 4-4 4"
private const val PLAY =
    "M6.51 18.87c.15.09.32.13.49.13s.36-.05.51-.14l10-6c.3-.18.49-.51.49-.86s-.18-.68-.49-.86l-10-6a.99.99 0 0 0-1." +
        "01-.01c-.31.18-.51.51-.51.87v12c0 .36.19.69.51.87Z"
private const val PLUS =
    "M11 17v4h2v-8h8v-2h-8V3h-2v8H3v2h8z"
private const val CHECK =
    "M9 15.59L4.71 11.3L3.3 12.71l5 5c.2.2.45.29.71.29s.51-.1.71-.29l11-11l-1.41-1.41L9.02 15.59Z"
private const val CHECK_CIRCLE =
    "M12 2C6.49 2 2 6.49 2 12s4.49 10 10 10s10-4.49 10-10S17.51 2 12 2m-1.29 13.71c-.2.2-.45.29-.71.29s-.51-.1-.71-" +
        ".29l-3-3L7.7 11.3l2.29 2.29l5.29-5.29l1.41 1.41l-6 6Z"
private const val CHECK_CIRCLE_OUTLINE =
    "M12 22C6.49 22 2 17.51 2 12S6.49 2 12 2s10 4.49 10 10s-4.49 10-10 10m0-18c-4.41 0-8 3.59-8 8s3.59 8 8 8s8-3.59" +
        " 8-8s-3.59-8-8-8M10 16c-.26 0-.51-.1-.71-.29l-3-3L7.7 11.3l2.29 2.29l5.29-5.29l1.41 1.41l-6 6c-.2.2-.45.29-.71" +
        ".29Z"
private const val STAR =
    "m6.87 14.33l-1.83 6.4c-.12.4.03.84.37 1.08c.34.25.8.26 1.14.02L12 18.2l5.45 3.63a.99.99 0 0 0 1.14-.02c.34-.25" +
        ".49-.68.37-1.08l-1.83-6.4l4.54-4.08c.3-.27.41-.69.28-1.06c-.13-.38-.47-.64-.87-.68l-5.7-.45l-2.47-5.46a.998.99" +
        "8 0 0 0-1.82 0L8.62 8.06l-5.7.45c-.4.03-.74.3-.87.68s-.02.8.28 1.06z"
private const val STAR_BORDER =
    "m4.83 12.49l2.04 1.83l-.83 2.9l-1 3.5c-.12.4.03.84.37 1.08c.34.25.8.26 1.14.02l3-2L12 18.19l2.45 1.63l3 2a.99." +
        "99 0 0 0 1.14-.02c.34-.25.49-.68.37-1.08l-1-3.5l-.83-2.9l2.04-1.83l2.5-2.25c.3-.27.41-.69.28-1.06c-.13-.38-.47" +
        "-.64-.87-.68l-3.15-.25l-2.56-.2l-2.47-5.46a.998.998 0 0 0-1.82 0L8.61 8.05l-2.56.2l-3.15.25c-.4.03-.74.3-.87.6" +
        "8s-.02.8.28 1.06l2.5 2.25Zm1.39-2.25l2.52-.2l.62-.05l.59-.05l.84-1.86l1.2-2.66l1.2 2.66l.84 1.86l.59.05l.62.05" +
        "l2.52.2l.83.07l-.77.69l-2.5 2.25l-.46.42l.17.6l1.25 4.38l-3.74-2.49l-.55-.37l-.55.37l-3.74 2.49l1.25-4.38l.17-" +
        ".6l-.46-.42L6.16 11l-.77-.69z"
private const val ARROW_BACK =
    "m6 12l6 5v-4h6v-2h-6V7z"
private const val CHEVRON_LEFT =
    "M14.29 6.29L8.59 12l5.7 5.71l1.42-1.42l-4.3-4.29l4.3-4.29z"
private const val CHEVRON_RIGHT =
    "m9.71 17.71l5.7-5.71l-5.7-5.71l-1.42 1.42l4.3 4.29l-4.3 4.29z"
private const val CHEVRON_DOWN =
    "m12 15.41l5.71-5.7l-1.42-1.42l-4.29 4.3l-4.29-4.3l-1.42 1.42z"
private const val CLOSE =
    "m7.76 14.83l-2.83 2.83l1.41 1.41l2.83-2.83l2.12-2.12l.71-.71l.71.71l1.41 1.42l3.54 3.53l1.41-1.41l-3.53-3.54l-" +
        "1.42-1.41l-.71-.71l5.66-5.66l-1.41-1.41L12 10.59L6.34 4.93L4.93 6.34L10.59 12l-.71.71z"
private const val PAUSE =
    "M6 18h4c.55 0 1-.45 1-1V7c0-.55-.45-1-1-1H6c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1M7 8h2v8H7zm7-2c-.55 0-1 .45-1 1" +
        "v10c0 .55.45 1 1 1h4c.55 0 1-.45 1-1V7c0-.55-.45-1-1-1zm3 10h-2V8h2z"
private const val REFRESH =
    "M19.07 4.93a9.9 9.9 0 0 0-3.18-2.14a10.12 10.12 0 0 0-7.79 0c-1.19.5-2.26 1.23-3.18 2.14S3.28 6.92 2.78 8.11A9" +
        ".95 9.95 0 0 0 1.99 12h2c0-1.08.21-2.13.63-3.11c.4-.95.98-1.81 1.72-2.54c.73-.74 1.59-1.31 2.54-1.71c1.97-.83 " +
        "4.26-.83 6.23 0c.95.4 1.81.98 2.54 1.72c.17.17.33.34.48.52L16 9.01h6V3l-2.45 2.45c-.15-.18-.31-.36-.48-.52m.3 " +
        "10.18c-.4.95-.98 1.81-1.72 2.54c-.73.74-1.59 1.31-2.54 1.71c-1.97.83-4.26.83-6.23 0c-.95-.4-1.81-.98-2.54-1.72" +
        "c-.17-.17-.33-.34-.48-.52l2.13-2.13H2v6l2.45-2.45c.15.18.31.36.48.52c.92.92 1.99 1.64 3.18 2.14c1.23.52 2.54.7" +
        "9 3.89.79s2.66-.26 3.89-.79c1.19-.5 2.26-1.23 3.18-2.14s1.64-1.99 2.14-3.18c.52-1.23.79-2.54.79-3.89h-2c0 1.08" +
        "-.21 2.13-.63 3.11Z"
private const val FORWARD =
    "m19.58 11.19l-7-5A.997.997 0 0 0 11 7v3.06L5.58 6.19A.997.997 0 0 0 4 7v10c0 .37.21.72.54.89c.14.07.3.11.46.11" +
        "c.2 0 .41-.06.58-.19L11 13.94V17c0 .37.21.72.54.89c.14.07.3.11.46.11c.2 0 .41-.06.58-.19l7-5c.26-.19.42-.49.42" +
        "-.81s-.16-.63-.42-.81M6 15.06V8.95l4.28 3.06L6 15.07Zm7 0V8.95l4.28 3.06L13 15.07Z"
private const val REPLAY =
    "M19.46 6.11c-.33-.17-.73-.14-1.04.08L13 10.06V7c0-.37-.21-.72-.54-.89s-.73-.14-1.04.08l-7 5c-.26.19-.42.49-.42" +
        ".81s.16.63.42.81l7 5c.17.12.38.19.58.19c.16 0 .31-.04.46-.11c.33-.17.54-.51.54-.89v-3.06l5.42 3.87c.17.12.38.1" +
        "9.58.19c.16 0 .31-.04.46-.11c.33-.17.54-.51.54-.89V7c0-.37-.21-.72-.54-.89M11 15.06L6.72 12L11 8.94v6.11Zm7 0L" +
        "13.72 12L18 8.94v6.11Z"
private const val FULLSCREEN =
    "M10 3H3v7h2V5h5zm0 16H5v-5H3v7h7zm11-5h-2v5h-5v2h7zm0-11h-7v2h5v5h2z"
private const val VOLUME =
    "M4 17h2.7l5.75 3.83c.17.11.36.17.55.17c.16 0 .32-.04.47-.12c.33-.17.53-.51.53-.88V4c0-.37-.2-.71-.53-.88c-.32-" +
        ".17-.72-.15-1.03.05L6.69 7h-2.7c-1.1 0-2 .9-2 2v6c0 1.1.9 2 2 2Zm0-8h3s.06-.02.09-.02c.06 0 .13-.02.19-.04a.6." +
        "6 0 0 0 .19-.08c.03-.01.06-.02.08-.03L12 5.87v12.26l-4.45-2.96s-.06-.02-.08-.03c-.06-.03-.12-.06-.19-.08c-.06-" +
        ".02-.13-.03-.19-.04c-.03 0-.06-.02-.09-.02H4zm18 3c0-4.09-2.47-7.61-6-9.16v2.24c2.39 1.39 4 3.96 4 6.92s-1.61 " +
        "5.53-4 6.92v2.24c3.53-1.55 6-5.07 6-9.16M18 12c0-1.77-.78-3.36-2-4.46v8.92c1.22-1.1 2-2.69 2-4.46"
private const val VISIBILITY =
    "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6M12 19c7.63 0 9.93-6.62 9.95-6.68c.07-.21.07-.43 0-.63c-.02-.07-2.32-6.68-9.9" +
        "5-6.68s-9.93 6.61-9.95 6.67c-.07.21-.07.43 0 .63c.02.07 2.32 6.68 9.95 6.68Zm0-12c5.35 0 7.42 3.85 7.93 5c-.5 " +
        "1.16-2.58 5-7.93 5s-7.42-3.84-7.93-5c.5-1.16 2.58-5 7.93-5"
private const val VISIBILITY_OFF =
    "M12 17c-5.35 0-7.42-3.84-7.93-5c.2-.46.65-1.34 1.45-2.23l-1.4-1.4c-1.49 1.65-2.06 3.28-2.08 3.31c-.07.21-.07.4" +
        "3 0 .63c.02.07 2.32 6.68 9.95 6.68c.91 0 1.73-.1 2.49-.26l-1.77-1.77c-.24.02-.47.03-.72.03Zm9.95-4.68c.07-.21." +
        "07-.43 0-.63c-.02-.07-2.32-6.68-9.95-6.68c-1.84 0-3.36.39-4.61.97L2.71 1.29L1.3 2.7l4.32 4.32l1.42 1.42l2.27 2" +
        ".27l3.98 3.98l1.8 1.8l1.53 1.53l4.68 4.68l1.41-1.41l-4.32-4.32c2.61-1.95 3.55-4.61 3.56-4.65m-7.25.97c.19-.39." +
        "3-.83.3-1.29c0-1.64-1.36-3-3-3c-.46 0-.89.11-1.29.3l-1.8-1.8c.88-.31 1.9-.5 3.08-.5c5.35 0 7.42 3.85 7.93 5c-." +
        "3.69-1.18 2.33-2.96 3.55z"
private const val SUBTITLES =
    "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2M4 18V6h16v12zM6 10h3v2H6zm5 0h7v2h" +
        "-7zm4 4h3v2h-3zm-9 0h7v2H6z"
private const val AUDIOTRACK =
    "M21 3H9c-.55 0-1 .45-1 1v9.56c-.59-.34-1.27-.56-2-.56c-2.21 0-4 1.79-4 4s1.79 4 4 4s4-1.79 4-4V5h10v8.56c-.59-" +
        ".34-1.27-.56-2-.56c-2.21 0-4 1.79-4 4s1.79 4 4 4s4-1.79 4-4V4c0-.55-.45-1-1-1M6 19c-1.1 0-2-.9-2-2s.9-2 2-2s2 " +
        ".9 2 2s-.9 2-2 2m12 0c-1.1 0-2-.9-2-2s.9-2 2-2s2 .9 2 2s-.9 2-2 2"
private const val INFO =
    "M11 11h2v6h-2zm0-4h2v2h-2zM12 22c5.51 0 10-4.49 10-10S17.51 2 12 2S2 6.49 2 12s4.49 10 10 10m0-18c4.41 0 8 3.5" +
        "9 8 8s-3.59 8-8 8s-8-3.59-8-8s3.59-8 8-8"
private const val ERROR =
    "M11 7h2v6h-2zm0 8h2v2h-2zM12 22c5.51 0 10-4.49 10-10S17.51 2 12 2S2 6.49 2 12s4.49 10 10 10m0-18c4.41 0 8 3.59" +
        " 8 8s-3.59 8-8 8s-8-3.59-8-8s3.59-8 8-8"
private const val COMPASS =
    "M12 2C6.49 2 2 6.49 2 12s4.49 10 10 10s10-4.49 10-10S17.51 2 12 2m0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8s8 3.59 8 " +
        "8s-3.59 8-8 8m8 16l5.99-2L16 8l-6 2z"
private const val FOLDER_OPEN =
    "M20 4h-8.59L10 2.59C9.62 2.21 9.12 2 8.59 2H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-" +
        "2-2m0 2v3H4V6zM4 18v-7h16v7z"
private const val INBOX =
    "M20 3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2m0 2v7h-3.42c-.4 0-.76.24-.92.6c-.6" +
        "4 1.46-2.08 2.4-3.66 2.4s-3.02-.94-3.66-2.4c-.16-.36-.52-.6-.92-.6H4V5zM4 19v-5h2.81c1.06 1.84 3.04 3 5.19 3s4" +
        ".13-1.16 5.19-3H20v5z"
private const val MOVIE =
    "M20 3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2M9.54 9L6.87 5h2.6l2.67 4zm5 0l-2.6" +
        "7-4h2.6l2.67 4zM4 5h.46l2.67 4H4zm0 14v-8h16V9h-.46l-2.67-4H20v14z"
private const val USER =
    "M12 12c2.76 0 5-2.24 5-5s-2.24-5-5-5s-5 2.24-5 5s2.24 5 5 5m0-8c1.65 0 3 1.35 3 3s-1.35 3-3 3s-3-1.35-3-3s1.35" +
        "-3 3-3M4 22h16c.55 0 1-.45 1-1v-1c0-3.86-3.14-7-7-7h-4c-3.86 0-7 3.14-7 7v1c0 .55.45 1 1 1m6-7h4c2.76 0 5 2.24" +
        " 5 5H5c0-2.76 2.24-5 5-5"
private const val SEARCH_OFF =
    "m17.06 14.94l-2.8-1.34A6.96 6.96 0 0 0 16 9c0-3.86-3.14-7-7-7S2 5.14 2 9s3.14 7 7 7c1.76 0 3.37-.66 4.6-1.74l1" +
        ".34 2.8l5 5l2.12-2.12zM9 14c-2.76 0-5-2.24-5-5s2.24-5 5-5s5 2.24 5 5s-2.24 5-5 5"
private const val CALENDAR_MONTH =
    "M19 4h-2V2h-2v2H9V2H7v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2M5 20V8h14V6v14z"
private const val COPY =
    "M20 2H10c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2m0 12H10V4h10zM14 20H4V10h2V8H4c-1" +
        ".1 0-2 .9-2 2v10c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2v-2h-2z"
private const val LOGIN =
    "M18 12c0 1.65-1.35 3-3 3H9v-3l-5 4l5 4v-3h6c2.76 0 5-2.24 5-5V4h-2z"
private const val LOGOUT =
    "M20 4c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2v16H2v2h20v-2h-2zm-2 0v16h-2V4zM6 20V4h8v16zM11 11h2v2h-2z"
