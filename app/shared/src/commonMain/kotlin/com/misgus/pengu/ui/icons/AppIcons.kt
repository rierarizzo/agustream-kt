package com.misgus.pengu.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Icon set vendored from Material Icons, Round theme (Apache License 2.0).
 *
 * Only the glyphs the app uses are kept, so the UI does not depend on
 * `material-icons-extended`. Each vector is built from the corresponding
 * Material Icons Round SVG path at the default 24dp size; call sites size it
 * with [com.misgus.pengu.ui.theme.AppIconSize].
 *
 * Names mirror the previous `Icons.*` references so call sites only swap the
 * receiver. `AutoMirrored` groups carry `autoMirror = true`, which is what the
 * Material auto-mirrored variants did.
 */
object AppIcons {
    /** Classic `Icons.Filled` / `Icons.Default`. */
    object Filled {
        val PlayArrow: ImageVector by lazy { rounded("PlayArrow", PLAY_ARROW) }
        val Close: ImageVector by lazy { rounded("Close", CLOSE) }
        val Search: ImageVector by lazy { rounded("Search", SEARCH) }
        val Settings: ImageVector by lazy { rounded("Settings", SETTINGS) }
        val Home: ImageVector by lazy { rounded("Home", HOME) }
        val Favorite: ImageVector by lazy { rounded("Favorite", FAVORITE) }
        val CalendarToday: ImageVector by lazy { rounded("CalendarToday", CALENDAR_TODAY) }
        val Refresh: ImageVector by lazy { rounded("Refresh", REFRESH) }
        val Check: ImageVector by lazy { rounded("Check", CHECK) }
        val ChevronRight: ImageVector by lazy { rounded("ChevronRight", CHEVRON_RIGHT) }
        val ChevronLeft: ImageVector by lazy { rounded("ChevronLeft", CHEVRON_LEFT) }
        val Star: ImageVector by lazy { rounded("Star", STAR) }
        val CheckCircle: ImageVector by lazy { rounded("CheckCircle", CHECK_CIRCLE) }
        val Pause: ImageVector by lazy { rounded("Pause", PAUSE) }
        val Replay10: ImageVector by lazy { rounded("Replay10", REPLAY_10) }
        val Forward10: ImageVector by lazy { rounded("Forward10", FORWARD_10) }
        val Fullscreen: ImageVector by lazy { rounded("Fullscreen", FULLSCREEN) }
        val ArrowDropDown: ImageVector by lazy { rounded("ArrowDropDown", ARROW_DROP_DOWN) }
        val Visibility: ImageVector by lazy { rounded("Visibility", VISIBILITY) }
        val VisibilityOff: ImageVector by lazy { rounded("VisibilityOff", VISIBILITY_OFF) }
    }

    /** Classic `Icons.Outlined`. */
    object Outlined {
        val ErrorOutline: ImageVector by lazy { rounded("ErrorOutline", ERROR_OUTLINE) }
        val Inbox: ImageVector by lazy { rounded("Inbox", INBOX) }
        val Explore: ImageVector by lazy { rounded("Explore", EXPLORE) }
        val Subtitles: ImageVector by lazy { rounded("Subtitles", SUBTITLES) }
        val StarBorder: ImageVector by lazy { rounded("StarBorder", STAR_BORDER) }
        val Settings: ImageVector by lazy { rounded("Settings", SETTINGS) }
        val SearchOff: ImageVector by lazy { rounded("SearchOff", SEARCH_OFF) }
        val Search: ImageVector by lazy { rounded("Search", SEARCH) }
        val Person: ImageVector by lazy { rounded("Person", PERSON) }
        val Movie: ImageVector by lazy { rounded("Movie", MOVIE) }
        val Info: ImageVector by lazy { rounded("Info", INFO) }
        val Home: ImageVector by lazy { rounded("Home", HOME) }
        val FolderOpen: ImageVector by lazy { rounded("FolderOpen", FOLDER_OPEN) }
        val ContentCopy: ImageVector by lazy { rounded("ContentCopy", CONTENT_COPY) }
        val CheckCircle: ImageVector by lazy { rounded("CheckCircle", CHECK_CIRCLE_OUTLINE) }
        val CalendarToday: ImageVector by lazy { rounded("CalendarToday", CALENDAR_TODAY) }
        val CalendarMonth: ImageVector by lazy { rounded("CalendarMonth", CALENDAR_MONTH) }
        val FavoriteBorder: ImageVector by lazy { rounded("FavoriteBorder", FAVORITE_BORDER) }
        val Audiotrack: ImageVector by lazy { rounded("Audiotrack", AUDIOTRACK) }
    }

    /** Classic `Icons.AutoMirrored.Filled`. */
    object AutoMirrored {
        object Filled {
            val ArrowBack: ImageVector by lazy { rounded("ArrowBack", ARROW_BACK, autoMirror = true) }
            val VolumeUp: ImageVector by lazy { rounded("VolumeUp", VOLUME_UP, autoMirror = true) }
        }

        object Outlined {
            val Login: ImageVector by lazy { rounded("Login", LOGIN, autoMirror = true) }
            val Logout: ImageVector by lazy { rounded("Logout", LOGOUT, autoMirror = true) }
        }
    }
}

/**
 * Builds one [ImageVector] from its Material Icons Round path.
 *
 * The path is parsed once, lazily, when the icon is first read.
 */
private fun rounded(
    name: String,
    pathData: String,
    fillType: PathFillType = PathFillType.NonZero,
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
                pathFillType = fillType,
                fill = SolidColor(Color.Black),
            )
        }.build()

private const val ARROW_BACK =
    "M19 11H7.83l4.88-4.88c.39-.39.39-1.03 0-1.42a.996.996 0 0 0-1.41 0l-6.59 6.59a.996.996 0 0 0 0 1.41l6.59 6.59a" +
        ".996.996 0 1 0 1.41-1.41L7.83 13H19c.55 0 1-.45 1-1s-.45-1-1-1z"
private const val ARROW_DROP_DOWN =
    "m8.71 11.71 2.59 2.59c.39.39 1.02.39 1.41 0l2.59-2.59c.63-.63.18-1.71-.71-1.71H9.41c-.89 0-1.33 1.08-.7 1.71z"
private const val AUDIOTRACK =
    "M12 5v8.55c-.94-.54-2.1-.75-3.33-.32-1.34.48-2.37 1.67-2.61 3.07a4.007 4.007 0 0 0 4.59 4.65c1.96-.31 3.35-2.1" +
        "1 3.35-4.1V7h2c1.1 0 2-.9 2-2s-.9-2-2-2h-2c-1.1 0-2 .9-2 2z"
private const val CALENDAR_MONTH =
    "M17 2c-.55 0-1 .45-1 1v1H8V3c0-.55-.45-1-1-1s-1 .45-1 1v1H5c-1.11 0-1.99.9-1.99 2L3 20a2 2 0 0 0 2 2h14c1.1 0 " +
        "2-.9 2-2V6c0-1.1-.9-2-2-2h-1V3c0-.55-.45-1-1-1zm2 18H5V10h14v10zm-8-7c0-.55.45-1 1-1s1 .45 1 1-.45 1-1 1-1-.45" +
        "-1-1zm-4 0c0-.55.45-1 1-1s1 .45 1 1-.45 1-1 1-1-.45-1-1zm8 0c0-.55.45-1 1-1s1 .45 1 1-.45 1-1 1-1-.45-1-1zm-4 " +
        "4c0-.55.45-1 1-1s1 .45 1 1-.45 1-1 1-1-.45-1-1zm-4 0c0-.55.45-1 1-1s1 .45 1 1-.45 1-1 1-1-.45-1-1zm8 0c0-.55.4" +
        "5-1 1-1s1 .45 1 1-.45 1-1 1-1-.45-1-1z"
private const val CALENDAR_TODAY =
    "M20 3h-1V2c0-.55-.45-1-1-1s-1 .45-1 1v1H7V2c0-.55-.45-1-1-1s-1 .45-1 1v1H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16" +
        "c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-1 18H5c-.55 0-1-.45-1-1V8h16v12c0 .55-.45 1-1 1z"
private const val CHECK =
    "M9 16.17 5.53 12.7a.996.996 0 1 0-1.41 1.41l4.18 4.18c.39.39 1.02.39 1.41 0L20.29 7.71a.996.996 0 1 0-1.41-1.4" +
        "1L9 16.17z"
private const val CHECK_CIRCLE =
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM9.29 16.29 5.7 12.7a.996.996 0 1 1 1.41-1.4" +
        "1L10 14.17l6.88-6.88a.996.996 0 1 1 1.41 1.41l-7.59 7.59a.996.996 0 0 1-1.41 0z"
private const val CHECK_CIRCLE_OUTLINE =
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8" +
        " 8-3.59 8-8 8zm3.88-11.71L10 14.17l-1.88-1.88a.996.996 0 1 0-1.41 1.41l2.59 2.59c.39.39 1.02.39 1.41 0L17.3 9." +
        "7a.996.996 0 0 0 0-1.41c-.39-.39-1.03-.39-1.42 0z"
private const val CHEVRON_LEFT =
    "M14.71 6.71a.996.996 0 0 0-1.41 0L8.71 11.3a.996.996 0 0 0 0 1.41l4.59 4.59a.996.996 0 1 0 1.41-1.41L10.83 12l" +
        "3.88-3.88c.39-.39.38-1.03 0-1.41z"
private const val CHEVRON_RIGHT =
    "M9.29 6.71a.996.996 0 0 0 0 1.41L13.17 12l-3.88 3.88a.996.996 0 1 0 1.41 1.41l4.59-4.59a.996.996 0 0 0 0-1.41L" +
        "10.7 6.7c-.38-.38-1.02-.38-1.41.01z"
private const val CLOSE =
    "M18.3 5.71a.996.996 0 0 0-1.41 0L12 10.59 7.11 5.7A.996.996 0 1 0 5.7 7.11L10.59 12 5.7 16.89a.996.996 0 1 0 1" +
        ".41 1.41L12 13.41l4.89 4.89a.996.996 0 1 0 1.41-1.41L13.41 12l4.89-4.89c.38-.38.38-1.02 0-1.4z"
private const val CONTENT_COPY =
    "M15 20H5V7c0-.55-.45-1-1-1s-1 .45-1 1v13c0 1.1.9 2 2 2h10c.55 0 1-.45 1-1s-.45-1-1-1zm5-4V4c0-1.1-.9-2-2-2H9c-" +
        "1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h9c1.1 0 2-.9 2-2zm-2 0H9V4h9v12z"
private const val ERROR_OUTLINE =
    "M12 7c.55 0 1 .45 1 1v4c0 .55-.45 1-1 1s-1-.45-1-1V8c0-.55.45-1 1-1zm-.01-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10" +
        "C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm1-3h-2v-2" +
        "h2v2z"
private const val EXPLORE =
    "M12 10.9c-.61 0-1.1.49-1.1 1.1s.49 1.1 1.1 1.1c.61 0 1.1-.49 1.1-1.1s-.49-1.1-1.1-1.1zM12 2C6.48 2 2 6.48 2 12" +
        "s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm2.19 12.19L6 18l3.81-8.19L18 6l-3.81 8.19z"
private const val FAVORITE =
    "M13.35 20.13c-.76.69-1.93.69-2.69-.01l-.11-.1C5.3 15.27 1.87 12.16 2 8.28c.06-1.7.93-3.33 2.34-4.29 2.64-1.8 5" +
        ".9-.96 7.66 1.1 1.76-2.06 5.02-2.91 7.66-1.1 1.41.96 2.28 2.59 2.34 4.29.14 3.88-3.3 6.99-8.55 11.76l-.1.09z"
private const val FAVORITE_BORDER =
    "M19.66 3.99c-2.64-1.8-5.9-.96-7.66 1.1-1.76-2.06-5.02-2.91-7.66-1.1-1.4.96-2.28 2.58-2.34 4.29-.14 3.88 3.3 6." +
        "99 8.55 11.76l.1.09c.76.69 1.93.69 2.69-.01l.11-.1c5.25-4.76 8.68-7.87 8.55-11.75-.06-1.7-.94-3.32-2.34-4.28zM" +
        "12.1 18.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14." +
        "96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"
private const val FOLDER_OPEN =
    "M20 6h-8l-1.41-1.41C10.21 4.21 9.7 4 9.17 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1." +
        "1-.9-2-2-2zm-1 12H5c-.55 0-1-.45-1-1V9c0-.55.45-1 1-1h14c.55 0 1 .45 1 1v8c0 .55-.45 1-1 1z"
private const val FORWARD_10 =
    "M18.92 13c-.5 0-.91.37-.98.86a6.006 6.006 0 0 1-7.42 4.96c-2.25-.54-3.91-2.27-4.39-4.53A6.014 6.014 0 0 1 12 7" +
        "v2.79c0 .45.54.67.85.35l3.79-3.79c.2-.2.2-.51 0-.71l-3.79-3.79a.5.5 0 0 0-.85.36V5c-4.94 0-8.84 4.48-7.84 9.6." +
        "6 3.11 2.9 5.5 5.99 6.19 4.83 1.08 9.15-2.2 9.77-6.67.09-.59-.4-1.12-1-1.12zm-8.02 3v-4.27h-.09l-1.77.63v.69l1" +
        ".01-.31V16zm3.42-4.22c-.18-.07-.37-.1-.59-.1s-.41.03-.59.1-.33.18-.45.33-.23.34-.29.57-.1.5-.1.82v.74c0 .32.04" +
        ".6.11.82s.17.42.3.57.28.26.46.33.37.1.59.1.41-.03.59-.1.33-.18.45-.33.22-.34.29-.57.1-.5.1-.82v-.74c0-.32-.04-" +
        ".6-.11-.82s-.17-.42-.3-.57-.29-.26-.46-.33zm.01 2.57c0 .19-.01.35-.04.48s-.06.24-.11.32-.11.14-.19.17-.16.05-." +
        "25.05-.18-.02-.25-.05-.14-.09-.19-.17-.09-.19-.12-.32-.04-.29-.04-.48v-.97c0-.19.01-.35.04-.48s.06-.23.12-.31." +
        "11-.14.19-.17.16-.05.25-.05.18.02.25.05.14.09.19.17.09.18.12.31.04.29.04.48v.97z"
private const val FULLSCREEN =
    "M6 14c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1h3c.55 0 1-.45 1-1s-.45-1-1-1H7v-2c0-.55-.45-1-1-1zm0-4c.55 0 1-.45 1-1" +
        "V7h2c.55 0 1-.45 1-1s-.45-1-1-1H6c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1zm11 7h-2c-.55 0-1 .45-1 1s.45 1 1 1h3c.55 " +
        "0 1-.45 1-1v-3c0-.55-.45-1-1-1s-1 .45-1 1v2zM14 6c0 .55.45 1 1 1h2v2c0 .55.45 1 1 1s1-.45 1-1V6c0-.55-.45-1-1-" +
        "1h-3c-.55 0-1 .45-1 1z"
private const val HOME =
    "M10 19v-5h4v5c0 .55.45 1 1 1h3c.55 0 1-.45 1-1v-7h1.7c.46 0 .68-.57.33-.87L12.67 3.6c-.38-.34-.96-.34-1.34 0l-" +
        "8.36 7.53c-.34.3-.13.87.33.87H5v7c0 .55.45 1 1 1h3c.55 0 1-.45 1-1z"
private const val INBOX =
    "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 2v9h-3.56c-.36 0-.68.19-.86.5-." +
        "52.9-1.47 1.5-2.58 1.5s-2.06-.6-2.58-1.5a1 1 0 0 0-.86-.5H5V5h14z"
private const val INFO =
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 15c-.55 0-1-.45-1-1v-4c0-.55.45-1 1-1s1 ." +
        "45 1 1v4c0 .55-.45 1-1 1zm1-8h-2V7h2v2z"
private const val LOGIN =
    "M10.3 7.7a.984.984 0 0 0 0 1.4l1.9 1.9H3c-.55 0-1 .45-1 1s.45 1 1 1h9.2l-1.9 1.9a.984.984 0 0 0 0 1.4c.39.39 1" +
        ".01.39 1.4 0l3.59-3.59a.996.996 0 0 0 0-1.41L11.7 7.7a.984.984 0 0 0-1.4 0zM20 19h-7c-.55 0-1 .45-1 1s.45 1 1 " +
        "1h7c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-7c-.55 0-1 .45-1 1s.45 1 1 1h7v14z"
private const val LOGOUT =
    "M5 5h6c.55 0 1-.45 1-1s-.45-1-1-1H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h6c.55 0 1-.45 1-1s-.45-1-1-1H5V5z"
private const val MOVIE =
    "m18 4 1.82 3.64c.08.16-.04.36-.22.36h-1.98c-.38 0-.73-.21-.89-.55L15 4h-2l1.82 3.64c.08.16-.04.36-.22.36h-1.98" +
        "c-.38 0-.73-.21-.89-.55L10 4H8l1.82 3.64c.08.16-.04.36-.22.36H7.62c-.38 0-.73-.21-.9-.55L5 4H4c-1.1 0-2 .9-2 2" +
        "v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-.55-.45-1-1-1h-3z"
private const val PAUSE =
    "M8 19c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2s-2 .9-2 2v10c0 1.1.9 2 2 2zm6-12v10c0 1.1.9 2 2 2s2-.9 2-2V7c0-1.1-.9-2-" +
        "2-2s-2 .9-2 2z"
private const val PERSON =
    "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v1c0 .55.45 1 1 1h14c.55 0 1" +
        "-.45 1-1v-1c0-2.66-5.33-4-8-4z"
private const val PLAY_ARROW =
    "M8 6.82v10.36c0 .79.87 1.27 1.54.84l8.14-5.18a1 1 0 0 0 0-1.69L9.54 5.98A.998.998 0 0 0 8 6.82z"
private const val REFRESH =
    "M17.65 6.35a7.95 7.95 0 0 0-6.48-2.31c-3.67.37-6.69 3.35-7.1 7.02C3.52 15.91 7.27 20 12 20a7.98 7.98 0 0 0 7.2" +
        "1-4.56c.32-.67-.16-1.44-.9-1.44-.37 0-.72.2-.88.53a5.994 5.994 0 0 1-6.8 3.31c-2.22-.49-4.01-2.3-4.48-4.52A6.0" +
        "02 6.002 0 0 1 12 6c1.66 0 3.14.69 4.22 1.78l-1.51 1.51c-.63.63-.19 1.71.7 1.71H19c.55 0 1-.45 1-1V6.41c0-.89-" +
        "1.08-1.34-1.71-.71l-.64.65z"
private const val REPLAY_10 =
    "M11.99 5V2.21c0-.45-.54-.67-.85-.35L7.35 5.65c-.2.2-.2.51 0 .71l3.79 3.79a.5.5 0 0 0 .85-.35V7c3.73 0 6.68 3.4" +
        "2 5.86 7.29-.47 2.27-2.31 4.1-4.57 4.57-3.57.75-6.75-1.7-7.23-5.01a.984.984 0 0 0-.98-.85c-.6 0-1.08.53-1 1.13" +
        ".62 4.39 4.8 7.64 9.53 6.72 3.12-.61 5.63-3.12 6.24-6.24.99-5.13-2.9-9.61-7.85-9.61zm-1.1 11h-.85v-3.26l-1.01." +
        "31v-.69l1.77-.63h.09V16zm4.28-1.76c0 .32-.03.6-.1.82s-.17.42-.29.57-.28.26-.45.33-.37.1-.59.1-.41-.03-.59-.1-." +
        "33-.18-.46-.33-.23-.34-.3-.57-.11-.5-.11-.82v-.74c0-.32.03-.6.1-.82s.17-.42.29-.57.28-.26.45-.33.37-.1.59-.1.4" +
        "1.03.59.1.33.18.46.33.23.34.3.57.11.5.11.82v.74zm-.85-.86c0-.19-.01-.35-.04-.48s-.07-.23-.12-.31-.11-.14-.19-." +
        "17-.16-.05-.25-.05-.18.02-.25.05-.14.09-.19.17-.09.18-.12.31-.04.29-.04.48v.97c0 .19.01.35.04.48s.07.24.12.32." +
        "11.14.19.17.16.05.25.05.18-.02.25-.05.14-.09.19-.17.09-.19.11-.32.04-.29.04-.48v-.97z"
private const val SEARCH =
    "M15.5 14h-.79l-.28-.27a6.5 6.5 0 0 0 1.48-5.34c-.47-2.78-2.79-5-5.59-5.34a6.505 6.505 0 0 0-7.27 7.27c.34 2.8 " +
        "2.56 5.12 5.34 5.59a6.5 6.5 0 0 0 5.34-1.48l.27.28v.79l4.25 4.25c.41.41 1.08.41 1.49 0 .41-.41.41-1.08 0-1.49L" +
        "15.5 14zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
private const val SEARCH_OFF =
    "M15.5 14h-.79l-.28-.27a6.5 6.5 0 0 0 1.48-5.34c-.47-2.78-2.79-4.99-5.58-5.34A6.492 6.492 0 0 0 3.03 9h2.02c.24" +
        "-2.12 1.92-3.8 4.06-3.98C11.65 4.8 14 6.95 14 9.5c0 2.49-2.01 4.5-4.5 4.5-.17 0-.33-.03-.5-.05v2.02l.01.01c1.8" +
        ".13 3.47-.47 4.72-1.55l.27.28v.79l4.25 4.25c.41.41 1.08.41 1.49 0 .41-.41.41-1.08 0-1.49L15.5 14z"
private const val SETTINGS =
    "M19.5 12c0-.23-.01-.45-.03-.68l1.86-1.41c.4-.3.51-.86.26-1.3l-1.87-3.23a.987.987 0 0 0-1.25-.42l-2.15.91c-.37-" +
        ".26-.76-.49-1.17-.68l-.29-2.31c-.06-.5-.49-.88-.99-.88h-3.73c-.51 0-.94.38-1 .88l-.29 2.31c-.41.19-.8.42-1.17." +
        "68l-2.15-.91c-.46-.2-1-.02-1.25.42L2.41 8.62c-.25.44-.14.99.26 1.3l1.86 1.41a7.343 7.343 0 0 0 0 1.35l-1.86 1." +
        "41c-.4.3-.51.86-.26 1.3l1.87 3.23c.25.44.79.62 1.25.42l2.15-.91c.37.26.76.49 1.17.68l.29 2.31c.06.5.49.88.99.8" +
        "8h3.73c.5 0 .93-.38.99-.88l.29-2.31c.41-.19.8-.42 1.17-.68l2.15.91c.46.2 1 .02 1.25-.42l1.87-3.23c.25-.44.14-." +
        "99-.26-1.3l-1.86-1.41c.03-.23.04-.45.04-.68zm-7.46 3.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 " +
        "3.5-1.57 3.5-3.5 3.5z"
private const val STAR =
    "m12 17.27 4.15 2.51c.76.46 1.69-.22 1.49-1.08l-1.1-4.72 3.67-3.18c.67-.58.31-1.68-.57-1.75l-4.83-.41-1.89-4.46" +
        "c-.34-.81-1.5-.81-1.84 0L9.19 8.63l-4.83.41c-.88.07-1.24 1.17-.57 1.75l3.67 3.18-1.1 4.72c-.2.86.73 1.54 1.49 " +
        "1.08l4.15-2.5z"
private const val STAR_BORDER =
    "m19.65 9.04-4.84-.42-1.89-4.45c-.34-.81-1.5-.81-1.84 0L9.19 8.63l-4.83.41c-.88.07-1.24 1.17-.57 1.75l3.67 3.18" +
        "-1.1 4.72c-.2.86.73 1.54 1.49 1.08l4.15-2.5 4.15 2.51c.76.46 1.69-.22 1.49-1.08l-1.1-4.73 3.67-3.18c.67-.58.32" +
        "-1.68-.56-1.75zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4" +
        "z"
private const val SUBTITLES =
    "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zM5 12h2c.55 0 1 .45 1 1s-.45 1-1 1" +
        "H5c-.55 0-1-.45-1-1s.45-1 1-1zm8 6H5c-.55 0-1-.45-1-1s.45-1 1-1h8c.55 0 1 .45 1 1s-.45 1-1 1zm6 0h-2c-.55 0-1-" +
        ".45-1-1s.45-1 1-1h2c.55 0 1 .45 1 1s-.45 1-1 1zm0-4h-8c-.55 0-1-.45-1-1s.45-1 1-1h8c.55 0 1 .45 1 1s-.45 1-1 1" +
        "z"
private const val VISIBILITY =
    "M12 4C7 4 2.73 7.11 1 11.5 2.73 15.89 7 19 12 19s9.27-3.11 11-7.5C21.27 7.11 17 4 12 4zm0 12.5c-2.76 0-5-2.24-" +
        "5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
private const val VISIBILITY_OFF =
    "M12 6.5c2.76 0 5 2.24 5 5 0 .51-.1 1-.24 1.46l3.06 3.06c1.39-1.23 2.49-2.77 3.18-4.53C21.27 7.11 17 4 12 4c-1." +
        "27 0-2.49.2-3.64.57l2.17 2.17c.47-.14.96-.24 1.47-.24zM2.71 3.16a.996.996 0 0 0 0 1.41l1.97 1.97A11.892 11.892" +
        " 0 0 0 1 11.5C2.73 15.89 7 19 12 19c1.52 0 2.97-.3 4.31-.82l2.72 2.72a.996.996 0 1 0 1.41-1.41L4.13 3.16c-.39-" +
        ".39-1.03-.39-1.42 0zM12 16.5c-2.76 0-5-2.24-5-5 0-.77.18-1.5.49-2.14l1.57 1.57c-.03.18-.06.37-.06.57 0 1.66 1." +
        "34 3 3 3 .2 0 .38-.03.57-.07L14.14 16c-.65.32-1.37.5-2.14.5zm2.97-5.33a2.97 2.97 0 0 0-2.64-2.64l2.64 2.64z"
private const val VOLUME_UP =
    "M3 10v4c0 .55.45 1 1 1h3l3.29 3.29c.63.63 1.71.18 1.71-.71V6.41c0-.89-1.08-1.34-1.71-.71L7 9H4c-.55 0-1 .45-1 " +
        "1zm13.5 2A4.5 4.5 0 0 0 14 7.97v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 4.45v.2c0 .38.25.71.6.85C17.18 6.53 19 9.0" +
        "6 19 12s-1.82 5.47-4.4 6.5c-.36.14-.6.47-.6.85v.2c0 .63.63 1.07 1.21.85C18.6 19.11 21 15.84 21 12s-2.4-7.11-5." +
        "79-8.4c-.58-.23-1.21.22-1.21.85z"
