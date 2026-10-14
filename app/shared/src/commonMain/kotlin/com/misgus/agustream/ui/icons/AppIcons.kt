package com.misgus.agustream.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon set vendored from Material Symbols (Apache License 2.0).
 *
 * Only the glyphs the app uses are kept, so the UI does not depend on
 * `material-icons-extended`. Each vector is generated from the corresponding
 * Material Symbols SVG at the default axes (fill 0/1, weight 400, grade 0,
 * optical size 24) and keeps a 24dp default size; call sites size it with
 * [com.misgus.agustream.ui.theme.AppIconSize].
 *
 * Names mirror the previous `Icons.*` references so call sites only swap the
 * receiver. `AutoMirrored` groups carry `autoMirror = true`, which is what the
 * Material auto-mirrored variants did.
 */
object AppIcons {
    /** Classic `Icons.Filled` / `Icons.Default`. */
    object Filled {
        val PlayArrow: ImageVector
            get() {
                if (cachedPlayArrow != null) return cachedPlayArrow!!
                cachedPlayArrow =
                    ImageVector
                        .Builder(
                            name = "PlayArrow",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(320f, 757f)
                                verticalLineToRelative(-560f)
                                lineToRelative(440f, 280f)
                                lineToRelative(-440f, 280f)
                                close()
                            }
                        }.build()
                return cachedPlayArrow!!
            }
        private var cachedPlayArrow: ImageVector? = null

        val Close: ImageVector
            get() {
                if (cachedClose != null) return cachedClose!!
                cachedClose =
                    ImageVector
                        .Builder(
                            name = "Close",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(249f, 753f)
                                lineToRelative(-42f, -42f)
                                lineToRelative(231f, -231f)
                                lineToRelative(-231f, -231f)
                                lineToRelative(42f, -42f)
                                lineToRelative(231f, 231f)
                                lineToRelative(231f, -231f)
                                lineToRelative(42f, 42f)
                                lineToRelative(-231f, 231f)
                                lineToRelative(231f, 231f)
                                lineToRelative(-42f, 42f)
                                lineToRelative(-231f, -231f)
                                lineToRelative(-231f, 231f)
                                close()
                            }
                        }.build()
                return cachedClose!!
            }
        private var cachedClose: ImageVector? = null

        val Search: ImageVector
            get() {
                if (cachedSearch != null) return cachedSearch!!
                cachedSearch =
                    ImageVector
                        .Builder(
                            name = "Search",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(796f, 839f)
                                lineTo(533f, 576f)
                                quadToRelative(-30f, 26f, -70f, 40.5f)
                                reflectiveQuadTo(378f, 631f)
                                quadToRelative(-108f, 0f, -183f, -75f)
                                reflectiveQuadToRelative(-75f, -181f)
                                quadToRelative(0f, -106f, 75f, -181f)
                                reflectiveQuadToRelative(182f, -75f)
                                quadToRelative(106f, 0f, 180.5f, 75f)
                                reflectiveQuadTo(632f, 375f)
                                quadToRelative(0f, 43f, -14f, 83f)
                                reflectiveQuadToRelative(-42f, 75f)
                                lineToRelative(264f, 262f)
                                lineToRelative(-44f, 44f)
                                close()
                                moveTo(377f, 571f)
                                quadToRelative(81f, 0f, 138f, -57.5f)
                                reflectiveQuadTo(572f, 375f)
                                quadToRelative(0f, -81f, -57f, -138.5f)
                                reflectiveQuadTo(377f, 179f)
                                quadToRelative(-82f, 0f, -139.5f, 57.5f)
                                reflectiveQuadTo(180f, 375f)
                                quadToRelative(0f, 81f, 57.5f, 138.5f)
                                reflectiveQuadTo(377f, 571f)
                                close()
                            }
                        }.build()
                return cachedSearch!!
            }
        private var cachedSearch: ImageVector? = null

        val Settings: ImageVector
            get() {
                if (cachedSettings != null) return cachedSettings!!
                cachedSettings =
                    ImageVector
                        .Builder(
                            name = "Settings",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(388f, 880f)
                                lineToRelative(-20f, -126f)
                                quadToRelative(-19f, -7f, -40f, -19f)
                                reflectiveQuadToRelative(-37f, -25f)
                                lineToRelative(-118f, 54f)
                                lineToRelative(-93f, -164f)
                                lineToRelative(108f, -79f)
                                quadToRelative(-2f, -9f, -2.5f, -20.5f)
                                reflectiveQuadTo(185f, 480f)
                                quadToRelative(0f, -9f, 0.5f, -20.5f)
                                reflectiveQuadTo(188f, 439f)
                                lineTo(80f, 360f)
                                lineToRelative(93f, -164f)
                                lineToRelative(118f, 54f)
                                quadToRelative(16f, -13f, 37f, -25f)
                                reflectiveQuadToRelative(40f, -18f)
                                lineToRelative(20f, -127f)
                                horizontalLineToRelative(184f)
                                lineToRelative(20f, 126f)
                                quadToRelative(19f, 7f, 40.5f, 18.5f)
                                reflectiveQuadTo(669f, 250f)
                                lineToRelative(118f, -54f)
                                lineToRelative(93f, 164f)
                                lineToRelative(-108f, 77f)
                                quadToRelative(2f, 10f, 2.5f, 21.5f)
                                reflectiveQuadToRelative(0.5f, 21.5f)
                                quadToRelative(0f, 10f, -0.5f, 21f)
                                reflectiveQuadToRelative(-2.5f, 21f)
                                lineToRelative(108f, 78f)
                                lineToRelative(-93f, 164f)
                                lineToRelative(-118f, -54f)
                                quadToRelative(-16f, 13f, -36.5f, 25.5f)
                                reflectiveQuadTo(592f, 754f)
                                lineTo(572f, 880f)
                                horizontalLineTo(388f)
                                close()
                                moveToRelative(92f, -270f)
                                quadToRelative(54f, 0f, 92f, -38f)
                                reflectiveQuadToRelative(38f, -92f)
                                quadToRelative(0f, -54f, -38f, -92f)
                                reflectiveQuadToRelative(-92f, -38f)
                                quadToRelative(-54f, 0f, -92f, 38f)
                                reflectiveQuadToRelative(-38f, 92f)
                                quadToRelative(0f, 54f, 38f, 92f)
                                reflectiveQuadToRelative(92f, 38f)
                                close()
                            }
                        }.build()
                return cachedSettings!!
            }
        private var cachedSettings: ImageVector? = null

        val Home: ImageVector
            get() {
                if (cachedHome != null) return cachedHome!!
                cachedHome =
                    ImageVector
                        .Builder(
                            name = "Home",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(160f, 840f)
                                verticalLineToRelative(-480f)
                                lineToRelative(320f, -240f)
                                lineToRelative(320f, 240f)
                                verticalLineToRelative(480f)
                                horizontalLineTo(560f)
                                verticalLineToRelative(-280f)
                                horizontalLineTo(400f)
                                verticalLineToRelative(280f)
                                horizontalLineTo(160f)
                                close()
                            }
                        }.build()
                return cachedHome!!
            }
        private var cachedHome: ImageVector? = null

        val Favorite: ImageVector
            get() {
                if (cachedFavorite != null) return cachedFavorite!!
                cachedFavorite =
                    ImageVector
                        .Builder(
                            name = "Favorite",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(480f, 839f)
                                lineToRelative(-41f, -37f)
                                quadToRelative(-106f, -97f, -175f, -167.5f)
                                reflectiveQuadToRelative(-110f, -126f)
                                quadTo(113f, 453f, 96.5f, 408f)
                                reflectiveQuadTo(80f, 317f)
                                quadToRelative(0f, -90f, 60.5f, -150.5f)
                                reflectiveQuadTo(290f, 106f)
                                quadToRelative(57f, 0f, 105.5f, 27f)
                                reflectiveQuadToRelative(84.5f, 78f)
                                quadToRelative(42f, -54f, 89f, -79.5f)
                                reflectiveQuadTo(670f, 106f)
                                quadToRelative(89f, 0f, 149.5f, 60.5f)
                                reflectiveQuadTo(880f, 317f)
                                quadToRelative(0f, 46f, -16.5f, 91f)
                                reflectiveQuadTo(806f, 508.5f)
                                quadToRelative(-41f, 55.5f, -110f, 126f)
                                reflectiveQuadTo(521f, 802f)
                                lineToRelative(-41f, 37f)
                                close()
                            }
                        }.build()
                return cachedFavorite!!
            }
        private var cachedFavorite: ImageVector? = null

        val CalendarToday: ImageVector
            get() {
                if (cachedCalendarToday != null) return cachedCalendarToday!!
                cachedCalendarToday =
                    ImageVector
                        .Builder(
                            name = "CalendarToday",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(180f, 880f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-620f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(340f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(65f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(620f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(180f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-430f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(430f)
                                close()
                            }
                        }.build()
                return cachedCalendarToday!!
            }
        private var cachedCalendarToday: ImageVector? = null

        val Refresh: ImageVector
            get() {
                if (cachedRefresh != null) return cachedRefresh!!
                cachedRefresh =
                    ImageVector
                        .Builder(
                            name = "Refresh",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(480f, 800f)
                                quadToRelative(-133f, 0f, -226.5f, -93.5f)
                                reflectiveQuadTo(160f, 480f)
                                quadToRelative(0f, -133f, 93.5f, -226.5f)
                                reflectiveQuadTo(480f, 160f)
                                quadToRelative(85f, 0f, 149f, 34.5f)
                                reflectiveQuadTo(740f, 289f)
                                verticalLineToRelative(-129f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(254f)
                                horizontalLineTo(546f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(168f)
                                quadToRelative(-38f, -60f, -97f, -97f)
                                reflectiveQuadToRelative(-137f, -37f)
                                quadToRelative(-109f, 0f, -184.5f, 75.5f)
                                reflectiveQuadTo(220f, 480f)
                                quadToRelative(0f, 109f, 75.5f, 184.5f)
                                reflectiveQuadTo(480f, 740f)
                                quadToRelative(83f, 0f, 152f, -47.5f)
                                reflectiveQuadTo(728f, 567f)
                                horizontalLineToRelative(62f)
                                quadToRelative(-29f, 105f, -115f, 169f)
                                reflectiveQuadToRelative(-195f, 64f)
                                close()
                            }
                        }.build()
                return cachedRefresh!!
            }
        private var cachedRefresh: ImageVector? = null

        val Check: ImageVector
            get() {
                if (cachedCheck != null) return cachedCheck!!
                cachedCheck =
                    ImageVector
                        .Builder(
                            name = "Check",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(378f, 714f)
                                lineTo(154f, 490f)
                                lineToRelative(43f, -43f)
                                lineToRelative(181f, 181f)
                                lineToRelative(384f, -384f)
                                lineToRelative(43f, 43f)
                                lineToRelative(-427f, 427f)
                                close()
                            }
                        }.build()
                return cachedCheck!!
            }
        private var cachedCheck: ImageVector? = null

        val ChevronRight: ImageVector
            get() {
                if (cachedChevronRight != null) return cachedChevronRight!!
                cachedChevronRight =
                    ImageVector
                        .Builder(
                            name = "ChevronRight",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(530f, 479f)
                                lineTo(332f, 281f)
                                lineToRelative(43f, -43f)
                                lineToRelative(241f, 241f)
                                lineToRelative(-241f, 241f)
                                lineToRelative(-43f, -43f)
                                lineToRelative(198f, -198f)
                                close()
                            }
                        }.build()
                return cachedChevronRight!!
            }
        private var cachedChevronRight: ImageVector? = null

        val ChevronLeft: ImageVector
            get() {
                if (cachedChevronLeft != null) return cachedChevronLeft!!
                cachedChevronLeft =
                    ImageVector
                        .Builder(
                            name = "ChevronLeft",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(561f, 720f)
                                lineTo(320f, 479f)
                                lineToRelative(241f, -241f)
                                lineToRelative(43f, 43f)
                                lineToRelative(-198f, 198f)
                                lineToRelative(198f, 198f)
                                lineToRelative(-43f, 43f)
                                close()
                            }
                        }.build()
                return cachedChevronLeft!!
            }
        private var cachedChevronLeft: ImageVector? = null

        val Star: ImageVector
            get() {
                if (cachedStar != null) return cachedStar!!
                cachedStar =
                    ImageVector
                        .Builder(
                            name = "Star",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(233f, 840f)
                                lineToRelative(65f, -281f)
                                lineTo(80f, 370f)
                                lineToRelative(288f, -25f)
                                lineToRelative(112f, -265f)
                                lineToRelative(112f, 265f)
                                lineToRelative(288f, 25f)
                                lineToRelative(-218f, 189f)
                                lineToRelative(65f, 281f)
                                lineToRelative(-247f, -149f)
                                lineToRelative(-247f, 149f)
                                close()
                            }
                        }.build()
                return cachedStar!!
            }
        private var cachedStar: ImageVector? = null

        val CheckCircle: ImageVector
            get() {
                if (cachedCheckCircle != null) return cachedCheckCircle!!
                cachedCheckCircle =
                    ImageVector
                        .Builder(
                            name = "CheckCircle",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(421f, 662f)
                                lineToRelative(283f, -283f)
                                lineToRelative(-46f, -45f)
                                lineToRelative(-237f, 237f)
                                lineToRelative(-120f, -120f)
                                lineToRelative(-45f, 45f)
                                lineToRelative(165f, 166f)
                                close()
                                moveToRelative(59f, 218f)
                                quadToRelative(-82f, 0f, -155f, -31.5f)
                                reflectiveQuadToRelative(-127.5f, -86f)
                                quadTo(143f, 708f, 111.5f, 635f)
                                reflectiveQuadTo(80f, 480f)
                                quadToRelative(0f, -83f, 31.5f, -156f)
                                reflectiveQuadToRelative(86f, -127f)
                                quadTo(252f, 143f, 325f, 111.5f)
                                reflectiveQuadTo(480f, 80f)
                                quadToRelative(83f, 0f, 156f, 31.5f)
                                reflectiveQuadTo(763f, 197f)
                                quadToRelative(54f, 54f, 85.5f, 127f)
                                reflectiveQuadTo(880f, 480f)
                                quadToRelative(0f, 82f, -31.5f, 155f)
                                reflectiveQuadTo(763f, 762.5f)
                                quadToRelative(-54f, 54.5f, -127f, 86f)
                                reflectiveQuadTo(480f, 880f)
                                close()
                            }
                        }.build()
                return cachedCheckCircle!!
            }
        private var cachedCheckCircle: ImageVector? = null

        val Pause: ImageVector
            get() {
                if (cachedPause != null) return cachedPause!!
                cachedPause =
                    ImageVector
                        .Builder(
                            name = "Pause",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(555f, 760f)
                                verticalLineToRelative(-560f)
                                horizontalLineToRelative(175f)
                                verticalLineToRelative(560f)
                                horizontalLineTo(555f)
                                close()
                                moveToRelative(-325f, 0f)
                                verticalLineToRelative(-560f)
                                horizontalLineToRelative(175f)
                                verticalLineToRelative(560f)
                                horizontalLineTo(230f)
                                close()
                            }
                        }.build()
                return cachedPause!!
            }
        private var cachedPause: ImageVector? = null

        val Replay10: ImageVector
            get() {
                if (cachedReplay10 != null) return cachedReplay10!!
                cachedReplay10 =
                    ImageVector
                        .Builder(
                            name = "Replay10",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(339.5f, 852f)
                                quadTo(274f, 824f, 225f, 775f)
                                reflectiveQuadToRelative(-77f, -114.5f)
                                quadTo(120f, 595f, 120f, 520f)
                                horizontalLineToRelative(60f)
                                quadToRelative(0f, 125f, 87.5f, 212.5f)
                                reflectiveQuadTo(480f, 820f)
                                quadToRelative(125f, 0f, 212.5f, -87.32f)
                                reflectiveQuadTo(780f, 520f)
                                quadToRelative(0f, -125.36f, -85f, -212.68f)
                                quadTo(610f, 220f, 485f, 220f)
                                horizontalLineToRelative(-22f)
                                lineToRelative(73f, 73f)
                                lineToRelative(-42f, 42f)
                                lineToRelative(-147f, -147f)
                                lineToRelative(147f, -147f)
                                lineToRelative(41f, 41f)
                                lineToRelative(-78f, 78f)
                                horizontalLineToRelative(23f)
                                quadToRelative(75f, 0f, 140.5f, 28f)
                                reflectiveQuadTo(735f, 265f)
                                quadToRelative(49f, 49f, 77f, 114.5f)
                                reflectiveQuadTo(840f, 520f)
                                quadToRelative(0f, 75f, -28f, 140.5f)
                                reflectiveQuadTo(735f, 775f)
                                quadToRelative(-49f, 49f, -114.5f, 77f)
                                reflectiveQuadTo(480f, 880f)
                                quadToRelative(-75f, 0f, -140.5f, -28f)
                                close()
                                moveTo(360f, 650f)
                                verticalLineToRelative(-212f)
                                horizontalLineToRelative(-54f)
                                verticalLineToRelative(-49f)
                                horizontalLineToRelative(104f)
                                verticalLineToRelative(261f)
                                horizontalLineToRelative(-50f)
                                close()
                                moveToRelative(147f, 0f)
                                quadToRelative(-18.7f, 0f, -31.35f, -12.65f)
                                quadTo(463f, 624.7f, 463f, 606f)
                                verticalLineToRelative(-173f)
                                quadToRelative(0f, -18.7f, 12.65f, -31.35f)
                                quadTo(488.3f, 389f, 507f, 389f)
                                horizontalLineToRelative(83f)
                                quadToRelative(18.7f, 0f, 31.35f, 12.65f)
                                quadTo(634f, 414.3f, 634f, 433f)
                                verticalLineToRelative(173f)
                                quadToRelative(0f, 18.7f, -12.65f, 31.35f)
                                quadTo(608.7f, 650f, 590f, 650f)
                                horizontalLineToRelative(-83f)
                                close()
                                moveToRelative(6f, -50f)
                                horizontalLineToRelative(71f)
                                verticalLineToRelative(-162f)
                                horizontalLineToRelative(-71f)
                                verticalLineToRelative(162f)
                                close()
                            }
                        }.build()
                return cachedReplay10!!
            }
        private var cachedReplay10: ImageVector? = null

        val Forward10: ImageVector
            get() {
                if (cachedForward10 != null) return cachedForward10!!
                cachedForward10 =
                    ImageVector
                        .Builder(
                            name = "Forward10",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(360f, 650f)
                                verticalLineToRelative(-212f)
                                horizontalLineToRelative(-54f)
                                verticalLineToRelative(-49f)
                                horizontalLineToRelative(104f)
                                verticalLineToRelative(261f)
                                horizontalLineToRelative(-50f)
                                close()
                                moveToRelative(147f, 0f)
                                quadToRelative(-18.7f, 0f, -31.35f, -12.65f)
                                quadTo(463f, 624.7f, 463f, 606f)
                                verticalLineToRelative(-173f)
                                quadToRelative(0f, -18.7f, 12.65f, -31.35f)
                                quadTo(488.3f, 389f, 507f, 389f)
                                horizontalLineToRelative(83f)
                                quadToRelative(18.7f, 0f, 31.35f, 12.65f)
                                quadTo(634f, 414.3f, 634f, 433f)
                                verticalLineToRelative(173f)
                                quadToRelative(0f, 18.7f, -12.65f, 31.35f)
                                quadTo(608.7f, 650f, 590f, 650f)
                                horizontalLineToRelative(-83f)
                                close()
                                moveToRelative(6f, -50f)
                                horizontalLineToRelative(71f)
                                verticalLineToRelative(-162f)
                                horizontalLineToRelative(-71f)
                                verticalLineToRelative(162f)
                                close()
                                moveTo(339.5f, 852f)
                                quadTo(274f, 824f, 225f, 775f)
                                reflectiveQuadToRelative(-77f, -114.5f)
                                quadTo(120f, 595f, 120f, 520f)
                                reflectiveQuadToRelative(28f, -140.5f)
                                quadTo(176f, 314f, 225f, 265f)
                                reflectiveQuadToRelative(114.5f, -77f)
                                quadTo(405f, 160f, 480f, 160f)
                                horizontalLineToRelative(21f)
                                lineToRelative(-78f, -78f)
                                lineToRelative(41f, -41f)
                                lineToRelative(147f, 147f)
                                lineToRelative(-147f, 147f)
                                lineToRelative(-41f, -41f)
                                lineToRelative(74f, -74f)
                                horizontalLineToRelative(-17f)
                                quadToRelative(-125.36f, 0f, -212.68f, 87.32f)
                                quadTo(180f, 394.64f, 180f, 520f)
                                quadToRelative(0f, 125.36f, 87.5f, 212.68f)
                                quadTo(355f, 820f, 480f, 820f)
                                reflectiveQuadToRelative(212.5f, -87.32f)
                                quadTo(780f, 645.36f, 780f, 520f)
                                horizontalLineToRelative(60f)
                                quadToRelative(0f, 75f, -28f, 140.5f)
                                reflectiveQuadTo(735f, 775f)
                                quadToRelative(-49f, 49f, -114.5f, 77f)
                                reflectiveQuadTo(480f, 880f)
                                quadToRelative(-75f, 0f, -140.5f, -28f)
                                close()
                            }
                        }.build()
                return cachedForward10!!
            }
        private var cachedForward10: ImageVector? = null

        val Fullscreen: ImageVector
            get() {
                if (cachedFullscreen != null) return cachedFullscreen!!
                cachedFullscreen =
                    ImageVector
                        .Builder(
                            name = "Fullscreen",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(120f, 840f)
                                verticalLineToRelative(-193f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(133f)
                                horizontalLineToRelative(133f)
                                verticalLineToRelative(60f)
                                horizontalLineTo(120f)
                                close()
                                moveToRelative(527f, 0f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(133f)
                                verticalLineToRelative(-133f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(193f)
                                horizontalLineTo(647f)
                                close()
                                moveTo(120f, 313f)
                                verticalLineToRelative(-193f)
                                horizontalLineToRelative(193f)
                                verticalLineToRelative(60f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(133f)
                                horizontalLineToRelative(-60f)
                                close()
                                moveToRelative(660f, 0f)
                                verticalLineToRelative(-133f)
                                horizontalLineTo(647f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(193f)
                                verticalLineToRelative(193f)
                                horizontalLineToRelative(-60f)
                                close()
                            }
                        }.build()
                return cachedFullscreen!!
            }
        private var cachedFullscreen: ImageVector? = null

        val ArrowDropDown: ImageVector
            get() {
                if (cachedArrowDropDown != null) return cachedArrowDropDown!!
                cachedArrowDropDown =
                    ImageVector
                        .Builder(
                            name = "ArrowDropDown",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(480f, 600f)
                                lineTo(280f, 401f)
                                horizontalLineToRelative(400f)
                                lineTo(480f, 600f)
                                close()
                            }
                        }.build()
                return cachedArrowDropDown!!
            }
        private var cachedArrowDropDown: ImageVector? = null

        val Visibility: ImageVector
            get() {
                if (cachedVisibility != null) return cachedVisibility!!
                cachedVisibility =
                    ImageVector
                        .Builder(
                            name = "Visibility",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(600.5f, 580.5f)
                                quadTo(650f, 531f, 650f, 460f)
                                reflectiveQuadToRelative(-49.5f, -120.5f)
                                quadTo(551f, 290f, 480f, 290f)
                                reflectiveQuadToRelative(-120.5f, 49.5f)
                                quadTo(310f, 389f, 310f, 460f)
                                reflectiveQuadToRelative(49.5f, 120.5f)
                                quadTo(409f, 630f, 480f, 630f)
                                reflectiveQuadToRelative(120.5f, -49.5f)
                                close()
                                moveToRelative(-200f, -41f)
                                quadTo(368f, 507f, 368f, 460f)
                                reflectiveQuadToRelative(32.5f, -79.5f)
                                quadTo(433f, 348f, 480f, 348f)
                                reflectiveQuadToRelative(79.5f, 32.5f)
                                quadTo(592f, 413f, 592f, 460f)
                                reflectiveQuadToRelative(-32.5f, 79.5f)
                                quadTo(527f, 572f, 480f, 572f)
                                reflectiveQuadToRelative(-79.5f, -32.5f)
                                close()
                                moveTo(216f, 677f)
                                quadTo(98f, 594f, 40f, 460f)
                                quadToRelative(58f, -134f, 176f, -217f)
                                reflectiveQuadToRelative(264f, -83f)
                                quadToRelative(146f, 0f, 264f, 83f)
                                reflectiveQuadToRelative(176f, 217f)
                                quadToRelative(-58f, 134f, -176f, 217f)
                                reflectiveQuadToRelative(-264f, 83f)
                                quadToRelative(-146f, 0f, -264f, -83f)
                                close()
                            }
                        }.build()
                return cachedVisibility!!
            }
        private var cachedVisibility: ImageVector? = null

        val VisibilityOff: ImageVector
            get() {
                if (cachedVisibilityOff != null) return cachedVisibilityOff!!
                cachedVisibilityOff =
                    ImageVector
                        .Builder(
                            name = "VisibilityOff",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(816f, 896f)
                                lineTo(648f, 731f)
                                quadToRelative(-35f, 14f, -79f, 21.5f)
                                reflectiveQuadToRelative(-89f, 7.5f)
                                quadToRelative(-146f, 0f, -265f, -81.5f)
                                reflectiveQuadTo(40f, 460f)
                                quadToRelative(20f, -52f, 55.5f, -101.5f)
                                reflectiveQuadTo(182f, 264f)
                                lineTo(56f, 138f)
                                lineToRelative(42f, -43f)
                                lineToRelative(757f, 757f)
                                lineToRelative(-39f, 44f)
                                close()
                                moveTo(480f, 630f)
                                quadToRelative(14f, 0f, 30f, -2.5f)
                                reflectiveQuadToRelative(27f, -7.5f)
                                lineTo(320f, 403f)
                                quadToRelative(-5f, 12f, -7.5f, 27f)
                                reflectiveQuadToRelative(-2.5f, 30f)
                                quadToRelative(0f, 72f, 50f, 121f)
                                reflectiveQuadToRelative(120f, 49f)
                                close()
                                moveToRelative(278f, 40f)
                                lineTo(629f, 541f)
                                quadToRelative(10f, -16f, 15.5f, -37.5f)
                                reflectiveQuadTo(650f, 460f)
                                quadToRelative(0f, -71f, -49.5f, -120.5f)
                                reflectiveQuadTo(480f, 290f)
                                quadToRelative(-22f, 0f, -43f, 5f)
                                reflectiveQuadToRelative(-38f, 16f)
                                lineTo(289f, 200f)
                                quadToRelative(35f, -16f, 89.5f, -28f)
                                reflectiveQuadTo(485f, 160f)
                                quadToRelative(143f, 0f, 261.5f, 81.5f)
                                reflectiveQuadTo(920f, 460f)
                                quadToRelative(-26f, 64f, -67f, 117f)
                                reflectiveQuadToRelative(-95f, 93f)
                                close()
                                moveTo(585f, 497f)
                                lineTo(443f, 355f)
                                quadToRelative(29f, -11f, 60f, -4.5f)
                                reflectiveQuadToRelative(54f, 28.5f)
                                quadToRelative(23f, 23f, 32f, 51.5f)
                                reflectiveQuadToRelative(-4f, 66.5f)
                                close()
                            }
                        }.build()
                return cachedVisibilityOff!!
            }
        private var cachedVisibilityOff: ImageVector? = null
    }

    /** Classic `Icons.Outlined`. */
    object Outlined {
        val ErrorOutline: ImageVector
            get() {
                if (cachedErrorOutline != null) return cachedErrorOutline!!
                cachedErrorOutline =
                    ImageVector
                        .Builder(
                            name = "ErrorOutline",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(503.5f, 670.52f)
                                quadToRelative(9.5f, -9.48f, 9.5f, -23.5f)
                                reflectiveQuadToRelative(-9.48f, -23.52f)
                                quadToRelative(-9.48f, -9.5f, -23.5f, -9.5f)
                                reflectiveQuadToRelative(-23.52f, 9.48f)
                                quadToRelative(-9.5f, 9.48f, -9.5f, 23.5f)
                                reflectiveQuadToRelative(9.48f, 23.52f)
                                quadToRelative(9.48f, 9.5f, 23.5f, 9.5f)
                                reflectiveQuadToRelative(23.52f, -9.48f)
                                close()
                                moveTo(453f, 527f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(-253f)
                                horizontalLineToRelative(-60f)
                                verticalLineToRelative(253f)
                                close()
                                moveToRelative(27.27f, 353f)
                                quadToRelative(-82.74f, 0f, -155.5f, -31.5f)
                                quadTo(252f, 817f, 197.5f, 762.5f)
                                reflectiveQuadToRelative(-86f, -127.34f)
                                quadTo(80f, 562.32f, 80f, 479.5f)
                                reflectiveQuadToRelative(31.5f, -155.66f)
                                quadTo(143f, 251f, 197.5f, 197f)
                                reflectiveQuadToRelative(127.34f, -85.5f)
                                quadTo(397.68f, 80f, 480.5f, 80f)
                                reflectiveQuadToRelative(155.66f, 31.5f)
                                quadTo(709f, 143f, 763f, 197f)
                                reflectiveQuadToRelative(85.5f, 127f)
                                quadTo(880f, 397f, 880f, 479.73f)
                                quadToRelative(0f, 82.74f, -31.5f, 155.5f)
                                quadTo(817f, 708f, 763f, 762.32f)
                                quadToRelative(-54f, 54.31f, -127f, 86f)
                                quadTo(563f, 880f, 480.27f, 880f)
                                close()
                                moveToRelative(0.23f, -60f)
                                quadTo(622f, 820f, 721f, 720.5f)
                                reflectiveQuadToRelative(99f, -241f)
                                quadTo(820f, 338f, 721.19f, 239f)
                                reflectiveQuadTo(480f, 140f)
                                quadToRelative(-141f, 0f, -240.5f, 98.81f)
                                reflectiveQuadTo(140f, 480f)
                                quadToRelative(0f, 141f, 99.5f, 240.5f)
                                reflectiveQuadToRelative(241f, 99.5f)
                                close()
                                moveToRelative(-0.5f, -340f)
                                close()
                            }
                        }.build()
                return cachedErrorOutline!!
            }
        private var cachedErrorOutline: ImageVector? = null

        val Inbox: ImageVector
            get() {
                if (cachedInbox != null) return cachedInbox!!
                cachedInbox =
                    ImageVector
                        .Builder(
                            name = "Inbox",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(180f, 840f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-600f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(600f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(600f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(180f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-136f)
                                horizontalLineTo(634f)
                                quadToRelative(-26f, 40f, -67.5f, 61.5f)
                                reflectiveQuadTo(480f, 727f)
                                quadToRelative(-45f, 0f, -86.5f, -21.5f)
                                reflectiveQuadTo(326f, 644f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(136f)
                                close()
                                moveToRelative(374f, -136.5f)
                                quadToRelative(33f, -23.5f, 56f, -59.5f)
                                horizontalLineToRelative(170f)
                                verticalLineToRelative(-404f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(404f)
                                horizontalLineToRelative(170f)
                                quadToRelative(23f, 36f, 56.25f, 59.5f)
                                quadToRelative(33.24f, 23.5f, 74f, 23.5f)
                                quadTo(521f, 667f, 554f, 643.5f)
                                close()
                                moveTo(180f, 780f)
                                horizontalLineToRelative(600f)
                                horizontalLineToRelative(-600f)
                                close()
                            }
                        }.build()
                return cachedInbox!!
            }
        private var cachedInbox: ImageVector? = null

        val Explore: ImageVector
            get() {
                if (cachedExplore != null) return cachedExplore!!
                cachedExplore =
                    ImageVector
                        .Builder(
                            name = "Explore",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(303f, 657f)
                                lineToRelative(270f, -83f)
                                lineToRelative(83f, -270f)
                                lineToRelative(-270f, 83f)
                                lineToRelative(-83f, 270f)
                                close()
                                moveToRelative(176.76f, -137f)
                                quadToRelative(-16.76f, 0f, -28.26f, -11.74f)
                                quadToRelative(-11.5f, -11.73f, -11.5f, -28.5f)
                                quadToRelative(0f, -16.76f, 11.74f, -28.26f)
                                quadToRelative(11.73f, -11.5f, 28.5f, -11.5f)
                                quadToRelative(16.76f, 0f, 28.26f, 11.74f)
                                quadToRelative(11.5f, 11.73f, 11.5f, 28.5f)
                                quadToRelative(0f, 16.76f, -11.74f, 28.26f)
                                quadToRelative(-11.73f, 11.5f, -28.5f, 11.5f)
                                close()
                                moveToRelative(0.51f, 360f)
                                quadToRelative(-82.74f, 0f, -155.5f, -31.5f)
                                quadTo(252f, 817f, 197.5f, 762.5f)
                                reflectiveQuadToRelative(-86f, -127.34f)
                                quadTo(80f, 562.32f, 80f, 479.5f)
                                reflectiveQuadToRelative(31.5f, -155.66f)
                                quadTo(143f, 251f, 197.5f, 197f)
                                reflectiveQuadToRelative(127.34f, -85.5f)
                                quadTo(397.68f, 80f, 480.5f, 80f)
                                reflectiveQuadToRelative(155.66f, 31.5f)
                                quadTo(709f, 143f, 763f, 197f)
                                reflectiveQuadToRelative(85.5f, 127f)
                                quadTo(880f, 397f, 880f, 479.73f)
                                quadToRelative(0f, 82.74f, -31.5f, 155.5f)
                                quadTo(817f, 708f, 763f, 762.32f)
                                quadToRelative(-54f, 54.31f, -127f, 86f)
                                quadTo(563f, 880f, 480.27f, 880f)
                                close()
                                moveToRelative(0.22f, -60f)
                                quadTo(622f, 820f, 721f, 720.51f)
                                quadToRelative(99f, -99.48f, 99f, -241f)
                                quadTo(820f, 338f, 721f, 239f)
                                reflectiveQuadToRelative(-240.51f, -99f)
                                quadToRelative(-141.52f, 0f, -241f, 99f)
                                quadTo(140f, 338f, 140f, 479.51f)
                                quadToRelative(0f, 141.52f, 99.49f, 241f)
                                quadToRelative(99.48f, 99.49f, 241f, 99.49f)
                                close()
                                moveTo(480f, 480f)
                                close()
                            }
                        }.build()
                return cachedExplore!!
            }
        private var cachedExplore: ImageVector? = null

        val Subtitles: ImageVector
            get() {
                if (cachedSubtitles != null) return cachedSubtitles!!
                cachedSubtitles =
                    ImageVector
                        .Builder(
                            name = "Subtitles",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(240f, 610f)
                                horizontalLineToRelative(360f)
                                verticalLineToRelative(-60f)
                                horizontalLineTo(240f)
                                verticalLineToRelative(60f)
                                close()
                                moveToRelative(420f, 0f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(-60f)
                                verticalLineToRelative(60f)
                                close()
                                moveTo(240f, 490f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(-60f)
                                verticalLineToRelative(60f)
                                close()
                                moveToRelative(120f, 0f)
                                horizontalLineToRelative(360f)
                                verticalLineToRelative(-60f)
                                horizontalLineTo(360f)
                                verticalLineToRelative(60f)
                                close()
                                moveTo(140f, 800f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-520f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(680f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(520f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(140f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(680f)
                                verticalLineToRelative(-520f)
                                horizontalLineTo(140f)
                                verticalLineToRelative(520f)
                                close()
                                moveToRelative(0f, 0f)
                                verticalLineToRelative(-520f)
                                verticalLineToRelative(520f)
                                close()
                            }
                        }.build()
                return cachedSubtitles!!
            }
        private var cachedSubtitles: ImageVector? = null

        val StarBorder: ImageVector
            get() {
                if (cachedStarBorder != null) return cachedStarBorder!!
                cachedStarBorder =
                    ImageVector
                        .Builder(
                            name = "StarBorder",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(323f, 715f)
                                lineToRelative(157f, -94f)
                                lineToRelative(157f, 95f)
                                lineToRelative(-42f, -178f)
                                lineToRelative(138f, -120f)
                                lineToRelative(-182f, -16f)
                                lineToRelative(-71f, -168f)
                                lineToRelative(-71f, 167f)
                                lineToRelative(-182f, 16f)
                                lineToRelative(138f, 120f)
                                lineToRelative(-42f, 178f)
                                close()
                                moveToRelative(-90f, 125f)
                                lineToRelative(65f, -281f)
                                lineTo(80f, 370f)
                                lineToRelative(288f, -25f)
                                lineToRelative(112f, -265f)
                                lineToRelative(112f, 265f)
                                lineToRelative(288f, 25f)
                                lineToRelative(-218f, 189f)
                                lineToRelative(65f, 281f)
                                lineToRelative(-247f, -149f)
                                lineToRelative(-247f, 149f)
                                close()
                                moveToRelative(247f, -355f)
                                close()
                            }
                        }.build()
                return cachedStarBorder!!
            }
        private var cachedStarBorder: ImageVector? = null

        val Settings: ImageVector
            get() {
                if (cachedSettings != null) return cachedSettings!!
                cachedSettings =
                    ImageVector
                        .Builder(
                            name = "Settings",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(388f, 880f)
                                lineToRelative(-20f, -126f)
                                quadToRelative(-19f, -7f, -40f, -19f)
                                reflectiveQuadToRelative(-37f, -25f)
                                lineToRelative(-118f, 54f)
                                lineToRelative(-93f, -164f)
                                lineToRelative(108f, -79f)
                                quadToRelative(-2f, -9f, -2.5f, -20.5f)
                                reflectiveQuadTo(185f, 480f)
                                quadToRelative(0f, -9f, 0.5f, -20.5f)
                                reflectiveQuadTo(188f, 439f)
                                lineTo(80f, 360f)
                                lineToRelative(93f, -164f)
                                lineToRelative(118f, 54f)
                                quadToRelative(16f, -13f, 37f, -25f)
                                reflectiveQuadToRelative(40f, -18f)
                                lineToRelative(20f, -127f)
                                horizontalLineToRelative(184f)
                                lineToRelative(20f, 126f)
                                quadToRelative(19f, 7f, 40.5f, 18.5f)
                                reflectiveQuadTo(669f, 250f)
                                lineToRelative(118f, -54f)
                                lineToRelative(93f, 164f)
                                lineToRelative(-108f, 77f)
                                quadToRelative(2f, 10f, 2.5f, 21.5f)
                                reflectiveQuadToRelative(0.5f, 21.5f)
                                quadToRelative(0f, 10f, -0.5f, 21f)
                                reflectiveQuadToRelative(-2.5f, 21f)
                                lineToRelative(108f, 78f)
                                lineToRelative(-93f, 164f)
                                lineToRelative(-118f, -54f)
                                quadToRelative(-16f, 13f, -36.5f, 25.5f)
                                reflectiveQuadTo(592f, 754f)
                                lineTo(572f, 880f)
                                horizontalLineTo(388f)
                                close()
                                moveToRelative(48f, -60f)
                                horizontalLineToRelative(88f)
                                lineToRelative(14f, -112f)
                                quadToRelative(33f, -8f, 62.5f, -25f)
                                reflectiveQuadToRelative(53.5f, -41f)
                                lineToRelative(106f, 46f)
                                lineToRelative(40f, -72f)
                                lineToRelative(-94f, -69f)
                                quadToRelative(4f, -17f, 6.5f, -33.5f)
                                reflectiveQuadTo(715f, 480f)
                                quadToRelative(0f, -17f, -2f, -33.5f)
                                reflectiveQuadToRelative(-7f, -33.5f)
                                lineToRelative(94f, -69f)
                                lineToRelative(-40f, -72f)
                                lineToRelative(-106f, 46f)
                                quadToRelative(-23f, -26f, -52f, -43.5f)
                                reflectiveQuadTo(538f, 252f)
                                lineToRelative(-14f, -112f)
                                horizontalLineToRelative(-88f)
                                lineToRelative(-14f, 112f)
                                quadToRelative(-34f, 7f, -63.5f, 24f)
                                reflectiveQuadTo(306f, 318f)
                                lineToRelative(-106f, -46f)
                                lineToRelative(-40f, 72f)
                                lineToRelative(94f, 69f)
                                quadToRelative(-4f, 17f, -6.5f, 33.5f)
                                reflectiveQuadTo(245f, 480f)
                                quadToRelative(0f, 17f, 2.5f, 33.5f)
                                reflectiveQuadTo(254f, 547f)
                                lineToRelative(-94f, 69f)
                                lineToRelative(40f, 72f)
                                lineToRelative(106f, -46f)
                                quadToRelative(24f, 24f, 53.5f, 41f)
                                reflectiveQuadToRelative(62.5f, 25f)
                                lineToRelative(14f, 112f)
                                close()
                                moveToRelative(44f, -210f)
                                quadToRelative(54f, 0f, 92f, -38f)
                                reflectiveQuadToRelative(38f, -92f)
                                quadToRelative(0f, -54f, -38f, -92f)
                                reflectiveQuadToRelative(-92f, -38f)
                                quadToRelative(-54f, 0f, -92f, 38f)
                                reflectiveQuadToRelative(-38f, 92f)
                                quadToRelative(0f, 54f, 38f, 92f)
                                reflectiveQuadToRelative(92f, 38f)
                                close()
                                moveToRelative(0f, -130f)
                                close()
                            }
                        }.build()
                return cachedSettings!!
            }
        private var cachedSettings: ImageVector? = null

        val SearchOff: ImageVector
            get() {
                if (cachedSearchOff != null) return cachedSearchOff!!
                cachedSearchOff =
                    ImageVector
                        .Builder(
                            name = "SearchOff",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(136f, 824f)
                                quadToRelative(-56f, -56f, -56f, -134f)
                                reflectiveQuadToRelative(56f, -134f)
                                quadToRelative(56f, -56f, 134f, -56f)
                                reflectiveQuadToRelative(134f, 56f)
                                quadToRelative(56f, 56f, 56f, 134f)
                                reflectiveQuadToRelative(-56f, 134f)
                                quadToRelative(-56f, 56f, -134f, 56f)
                                reflectiveQuadToRelative(-134f, -56f)
                                close()
                                moveToRelative(700f, 16f)
                                lineTo(573f, 577f)
                                quadToRelative(-14f, 11f, -31.5f, 21.5f)
                                reflectiveQuadTo(508f, 616f)
                                quadToRelative(-5f, -14f, -11f, -28.5f)
                                reflectiveQuadTo(483f, 561f)
                                quadToRelative(54f, -21f, 91.5f, -69.5f)
                                reflectiveQuadTo(612f, 376f)
                                quadToRelative(0f, -81f, -57f, -138.5f)
                                reflectiveQuadTo(417f, 180f)
                                quadToRelative(-82f, 0f, -139.5f, 57.5f)
                                reflectiveQuadTo(220f, 376f)
                                quadToRelative(0f, 17f, 3.5f, 35.5f)
                                reflectiveQuadTo(232f, 443f)
                                quadToRelative(-13f, 2f, -29f, 6.5f)
                                reflectiveQuadTo(174f, 460f)
                                quadToRelative(-7f, -18f, -10.5f, -40f)
                                reflectiveQuadToRelative(-3.5f, -44f)
                                quadToRelative(0f, -107f, 75f, -181.5f)
                                reflectiveQuadTo(417f, 120f)
                                quadToRelative(106f, 0f, 180.5f, 75f)
                                reflectiveQuadTo(672f, 376f)
                                quadToRelative(0f, 43f, -15f, 85f)
                                reflectiveQuadToRelative(-41f, 73f)
                                lineToRelative(264f, 262f)
                                lineToRelative(-44f, 44f)
                                close()
                                moveToRelative(-635f, -56f)
                                lineToRelative(69f, -69f)
                                lineToRelative(68f, 68f)
                                lineToRelative(23f, -23f)
                                lineToRelative(-69f, -69f)
                                lineToRelative(71f, -71f)
                                lineToRelative(-23f, -23f)
                                lineToRelative(-70f, 70f)
                                lineToRelative(-70f, -70f)
                                lineToRelative(-23f, 23f)
                                lineToRelative(70f, 70f)
                                lineToRelative(-70f, 70f)
                                lineToRelative(24f, 24f)
                                close()
                            }
                        }.build()
                return cachedSearchOff!!
            }
        private var cachedSearchOff: ImageVector? = null

        val Search: ImageVector
            get() {
                if (cachedSearch != null) return cachedSearch!!
                cachedSearch =
                    ImageVector
                        .Builder(
                            name = "Search",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(796f, 839f)
                                lineTo(533f, 576f)
                                quadToRelative(-30f, 26f, -70f, 40.5f)
                                reflectiveQuadTo(378f, 631f)
                                quadToRelative(-108f, 0f, -183f, -75f)
                                reflectiveQuadToRelative(-75f, -181f)
                                quadToRelative(0f, -106f, 75f, -181f)
                                reflectiveQuadToRelative(182f, -75f)
                                quadToRelative(106f, 0f, 180.5f, 75f)
                                reflectiveQuadTo(632f, 375f)
                                quadToRelative(0f, 43f, -14f, 83f)
                                reflectiveQuadToRelative(-42f, 75f)
                                lineToRelative(264f, 262f)
                                lineToRelative(-44f, 44f)
                                close()
                                moveTo(377f, 571f)
                                quadToRelative(81f, 0f, 138f, -57.5f)
                                reflectiveQuadTo(572f, 375f)
                                quadToRelative(0f, -81f, -57f, -138.5f)
                                reflectiveQuadTo(377f, 179f)
                                quadToRelative(-82f, 0f, -139.5f, 57.5f)
                                reflectiveQuadTo(180f, 375f)
                                quadToRelative(0f, 81f, 57.5f, 138.5f)
                                reflectiveQuadTo(377f, 571f)
                                close()
                            }
                        }.build()
                return cachedSearch!!
            }
        private var cachedSearch: ImageVector? = null

        val Person: ImageVector
            get() {
                if (cachedPerson != null) return cachedPerson!!
                cachedPerson =
                    ImageVector
                        .Builder(
                            name = "Person",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(372f, 437f)
                                quadToRelative(-42f, -42f, -42f, -108f)
                                reflectiveQuadToRelative(42f, -108f)
                                quadToRelative(42f, -42f, 108f, -42f)
                                reflectiveQuadToRelative(108f, 42f)
                                quadToRelative(42f, 42f, 42f, 108f)
                                reflectiveQuadToRelative(-42f, 108f)
                                quadToRelative(-42f, 42f, -108f, 42f)
                                reflectiveQuadToRelative(-108f, -42f)
                                close()
                                moveTo(160f, 800f)
                                verticalLineToRelative(-94f)
                                quadToRelative(0f, -38f, 19f, -65f)
                                reflectiveQuadToRelative(49f, -41f)
                                quadToRelative(67f, -30f, 128.5f, -45f)
                                reflectiveQuadTo(480f, 540f)
                                quadToRelative(62f, 0f, 123f, 15.5f)
                                reflectiveQuadTo(731f, 600f)
                                quadToRelative(31f, 14f, 50f, 41f)
                                reflectiveQuadToRelative(19f, 65f)
                                verticalLineToRelative(94f)
                                horizontalLineTo(160f)
                                close()
                                moveToRelative(60f, -60f)
                                horizontalLineToRelative(520f)
                                verticalLineToRelative(-34f)
                                quadToRelative(0f, -16f, -9.5f, -30.5f)
                                reflectiveQuadTo(707f, 654f)
                                quadToRelative(-64f, -31f, -117f, -42.5f)
                                reflectiveQuadTo(480f, 600f)
                                quadToRelative(-57f, 0f, -111f, 11.5f)
                                reflectiveQuadTo(252f, 654f)
                                quadToRelative(-14f, 7f, -23f, 21.5f)
                                reflectiveQuadToRelative(-9f, 30.5f)
                                verticalLineToRelative(34f)
                                close()
                                moveToRelative(324.5f, -346.5f)
                                quadTo(570f, 368f, 570f, 329f)
                                reflectiveQuadToRelative(-25.5f, -64.5f)
                                quadTo(519f, 239f, 480f, 239f)
                                reflectiveQuadToRelative(-64.5f, 25.5f)
                                quadTo(390f, 290f, 390f, 329f)
                                reflectiveQuadToRelative(25.5f, 64.5f)
                                quadTo(441f, 419f, 480f, 419f)
                                reflectiveQuadToRelative(64.5f, -25.5f)
                                close()
                                moveTo(480f, 329f)
                                close()
                                moveToRelative(0f, 411f)
                                close()
                            }
                        }.build()
                return cachedPerson!!
            }
        private var cachedPerson: ImageVector? = null

        val Movie: ImageVector
            get() {
                if (cachedMovie != null) return cachedMovie!!
                cachedMovie =
                    ImageVector
                        .Builder(
                            name = "Movie",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(140f, 160f)
                                lineToRelative(74f, 152f)
                                horizontalLineToRelative(130f)
                                lineToRelative(-74f, -152f)
                                horizontalLineToRelative(89f)
                                lineToRelative(74f, 152f)
                                horizontalLineToRelative(130f)
                                lineToRelative(-74f, -152f)
                                horizontalLineToRelative(89f)
                                lineToRelative(74f, 152f)
                                horizontalLineToRelative(130f)
                                lineToRelative(-74f, -152f)
                                horizontalLineToRelative(112f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(520f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(140f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-520f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                close()
                                moveToRelative(0f, 212f)
                                verticalLineToRelative(368f)
                                horizontalLineToRelative(680f)
                                verticalLineToRelative(-368f)
                                horizontalLineTo(140f)
                                close()
                                moveToRelative(0f, 0f)
                                verticalLineToRelative(368f)
                                verticalLineToRelative(-368f)
                                close()
                            }
                        }.build()
                return cachedMovie!!
            }
        private var cachedMovie: ImageVector? = null

        val Info: ImageVector
            get() {
                if (cachedInfo != null) return cachedInfo!!
                cachedInfo =
                    ImageVector
                        .Builder(
                            name = "Info",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(453f, 680f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(-240f)
                                horizontalLineToRelative(-60f)
                                verticalLineToRelative(240f)
                                close()
                                moveToRelative(50.5f, -323.2f)
                                quadToRelative(9.5f, -9.2f, 9.5f, -22.8f)
                                quadToRelative(0f, -14.45f, -9.48f, -24.22f)
                                quadToRelative(-9.48f, -9.78f, -23.5f, -9.78f)
                                reflectiveQuadToRelative(-23.52f, 9.78f)
                                quadTo(447f, 319.55f, 447f, 334f)
                                quadToRelative(0f, 13.6f, 9.48f, 22.8f)
                                quadToRelative(9.48f, 9.2f, 23.5f, 9.2f)
                                reflectiveQuadToRelative(23.52f, -9.2f)
                                close()
                                moveTo(480.27f, 880f)
                                quadToRelative(-82.74f, 0f, -155.5f, -31.5f)
                                quadTo(252f, 817f, 197.5f, 762.5f)
                                reflectiveQuadToRelative(-86f, -127.34f)
                                quadTo(80f, 562.32f, 80f, 479.5f)
                                reflectiveQuadToRelative(31.5f, -155.66f)
                                quadTo(143f, 251f, 197.5f, 197f)
                                reflectiveQuadToRelative(127.34f, -85.5f)
                                quadTo(397.68f, 80f, 480.5f, 80f)
                                reflectiveQuadToRelative(155.66f, 31.5f)
                                quadTo(709f, 143f, 763f, 197f)
                                reflectiveQuadToRelative(85.5f, 127f)
                                quadTo(880f, 397f, 880f, 479.73f)
                                quadToRelative(0f, 82.74f, -31.5f, 155.5f)
                                quadTo(817f, 708f, 763f, 762.32f)
                                quadToRelative(-54f, 54.31f, -127f, 86f)
                                quadTo(563f, 880f, 480.27f, 880f)
                                close()
                                moveToRelative(0.23f, -60f)
                                quadTo(622f, 820f, 721f, 720.5f)
                                reflectiveQuadToRelative(99f, -241f)
                                quadTo(820f, 338f, 721.19f, 239f)
                                reflectiveQuadTo(480f, 140f)
                                quadToRelative(-141f, 0f, -240.5f, 98.81f)
                                reflectiveQuadTo(140f, 480f)
                                quadToRelative(0f, 141f, 99.5f, 240.5f)
                                reflectiveQuadToRelative(241f, 99.5f)
                                close()
                                moveToRelative(-0.5f, -340f)
                                close()
                            }
                        }.build()
                return cachedInfo!!
            }
        private var cachedInfo: ImageVector? = null

        val Home: ImageVector
            get() {
                if (cachedHome != null) return cachedHome!!
                cachedHome =
                    ImageVector
                        .Builder(
                            name = "Home",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(220f, 780f)
                                horizontalLineToRelative(150f)
                                verticalLineToRelative(-250f)
                                horizontalLineToRelative(220f)
                                verticalLineToRelative(250f)
                                horizontalLineToRelative(150f)
                                verticalLineToRelative(-390f)
                                lineTo(480f, 195f)
                                lineTo(220f, 390f)
                                verticalLineToRelative(390f)
                                close()
                                moveToRelative(-60f, 60f)
                                verticalLineToRelative(-480f)
                                lineToRelative(320f, -240f)
                                lineToRelative(320f, 240f)
                                verticalLineToRelative(480f)
                                horizontalLineTo(530f)
                                verticalLineToRelative(-250f)
                                horizontalLineTo(430f)
                                verticalLineToRelative(250f)
                                horizontalLineTo(160f)
                                close()
                                moveToRelative(320f, -353f)
                                close()
                            }
                        }.build()
                return cachedHome!!
            }
        private var cachedHome: ImageVector? = null

        val FolderOpen: ImageVector
            get() {
                if (cachedFolderOpen != null) return cachedFolderOpen!!
                cachedFolderOpen =
                    ImageVector
                        .Builder(
                            name = "FolderOpen",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(140f, 800f)
                                quadToRelative(-23f, 0f, -41.5f, -18.5f)
                                reflectiveQuadTo(80f, 740f)
                                verticalLineToRelative(-520f)
                                quadToRelative(0f, -23f, 18.5f, -41.5f)
                                reflectiveQuadTo(140f, 160f)
                                horizontalLineToRelative(281f)
                                lineToRelative(60f, 60f)
                                horizontalLineToRelative(339f)
                                quadToRelative(23f, 0f, 41.5f, 18.5f)
                                reflectiveQuadTo(880f, 280f)
                                horizontalLineTo(455f)
                                lineToRelative(-60f, -60f)
                                horizontalLineTo(140f)
                                verticalLineToRelative(520f)
                                lineToRelative(102f, -400f)
                                horizontalLineToRelative(698f)
                                lineTo(833f, 754f)
                                quadToRelative(-6f, 24f, -22f, 35f)
                                reflectiveQuadToRelative(-41f, 11f)
                                horizontalLineTo(140f)
                                close()
                                moveToRelative(63f, -60f)
                                horizontalLineToRelative(572f)
                                lineToRelative(84f, -340f)
                                horizontalLineTo(287f)
                                lineToRelative(-84f, 340f)
                                close()
                                moveToRelative(0f, 0f)
                                lineToRelative(84f, -340f)
                                lineToRelative(-84f, 340f)
                                close()
                                moveToRelative(-63f, -460f)
                                verticalLineToRelative(-60f)
                                verticalLineToRelative(60f)
                                close()
                            }
                        }.build()
                return cachedFolderOpen!!
            }
        private var cachedFolderOpen: ImageVector? = null

        val ContentCopy: ImageVector
            get() {
                if (cachedContentCopy != null) return cachedContentCopy!!
                cachedContentCopy =
                    ImageVector
                        .Builder(
                            name = "ContentCopy",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(300f, 760f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-560f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(440f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(560f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(300f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(440f)
                                verticalLineToRelative(-560f)
                                horizontalLineTo(300f)
                                verticalLineToRelative(560f)
                                close()
                                moveTo(180f, 880f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-620f)
                                horizontalLineToRelative(60f)
                                verticalLineToRelative(620f)
                                horizontalLineToRelative(500f)
                                verticalLineToRelative(60f)
                                horizontalLineTo(180f)
                                close()
                                moveToRelative(120f, -180f)
                                verticalLineToRelative(-560f)
                                verticalLineToRelative(560f)
                                close()
                            }
                        }.build()
                return cachedContentCopy!!
            }
        private var cachedContentCopy: ImageVector? = null

        val CheckCircle: ImageVector
            get() {
                if (cachedCheckCircle != null) return cachedCheckCircle!!
                cachedCheckCircle =
                    ImageVector
                        .Builder(
                            name = "CheckCircle",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(421f, 662f)
                                lineToRelative(283f, -283f)
                                lineToRelative(-46f, -45f)
                                lineToRelative(-237f, 237f)
                                lineToRelative(-120f, -120f)
                                lineToRelative(-45f, 45f)
                                lineToRelative(165f, 166f)
                                close()
                                moveToRelative(59f, 218f)
                                quadToRelative(-82f, 0f, -155f, -31.5f)
                                reflectiveQuadToRelative(-127.5f, -86f)
                                quadTo(143f, 708f, 111.5f, 635f)
                                reflectiveQuadTo(80f, 480f)
                                quadToRelative(0f, -83f, 31.5f, -156f)
                                reflectiveQuadToRelative(86f, -127f)
                                quadTo(252f, 143f, 325f, 111.5f)
                                reflectiveQuadTo(480f, 80f)
                                quadToRelative(83f, 0f, 156f, 31.5f)
                                reflectiveQuadTo(763f, 197f)
                                quadToRelative(54f, 54f, 85.5f, 127f)
                                reflectiveQuadTo(880f, 480f)
                                quadToRelative(0f, 82f, -31.5f, 155f)
                                reflectiveQuadTo(763f, 762.5f)
                                quadToRelative(-54f, 54.5f, -127f, 86f)
                                reflectiveQuadTo(480f, 880f)
                                close()
                                moveToRelative(0f, -60f)
                                quadToRelative(142f, 0f, 241f, -99.5f)
                                reflectiveQuadTo(820f, 480f)
                                quadToRelative(0f, -142f, -99f, -241f)
                                reflectiveQuadToRelative(-241f, -99f)
                                quadToRelative(-141f, 0f, -240.5f, 99f)
                                reflectiveQuadTo(140f, 480f)
                                quadToRelative(0f, 141f, 99.5f, 240.5f)
                                reflectiveQuadTo(480f, 820f)
                                close()
                                moveToRelative(0f, -340f)
                                close()
                            }
                        }.build()
                return cachedCheckCircle!!
            }
        private var cachedCheckCircle: ImageVector? = null

        val CalendarToday: ImageVector
            get() {
                if (cachedCalendarToday != null) return cachedCalendarToday!!
                cachedCalendarToday =
                    ImageVector
                        .Builder(
                            name = "CalendarToday",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(180f, 880f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-620f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(340f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(65f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(620f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(180f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-430f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(430f)
                                close()
                                moveToRelative(0f, -490f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-130f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(130f)
                                close()
                                moveToRelative(0f, 0f)
                                verticalLineToRelative(-130f)
                                verticalLineToRelative(130f)
                                close()
                            }
                        }.build()
                return cachedCalendarToday!!
            }
        private var cachedCalendarToday: ImageVector? = null

        val CalendarMonth: ImageVector
            get() {
                if (cachedCalendarMonth != null) return cachedCalendarMonth!!
                cachedCalendarMonth =
                    ImageVector
                        .Builder(
                            name = "CalendarMonth",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(180f, 880f)
                                quadToRelative(-24f, 0f, -42f, -18f)
                                reflectiveQuadToRelative(-18f, -42f)
                                verticalLineToRelative(-620f)
                                quadToRelative(0f, -24f, 18f, -42f)
                                reflectiveQuadToRelative(42f, -18f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(340f)
                                verticalLineToRelative(-60f)
                                horizontalLineToRelative(65f)
                                verticalLineToRelative(60f)
                                horizontalLineToRelative(65f)
                                quadToRelative(24f, 0f, 42f, 18f)
                                reflectiveQuadToRelative(18f, 42f)
                                verticalLineToRelative(620f)
                                quadToRelative(0f, 24f, -18f, 42f)
                                reflectiveQuadToRelative(-42f, 18f)
                                horizontalLineTo(180f)
                                close()
                                moveToRelative(0f, -60f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-430f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(430f)
                                close()
                                moveToRelative(0f, -490f)
                                horizontalLineToRelative(600f)
                                verticalLineToRelative(-130f)
                                horizontalLineTo(180f)
                                verticalLineToRelative(130f)
                                close()
                                moveToRelative(0f, 0f)
                                verticalLineToRelative(-130f)
                                verticalLineToRelative(130f)
                                close()
                                moveToRelative(300f, 230f)
                                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                                reflectiveQuadTo(440f, 520f)
                                quadToRelative(0f, -17f, 11.5f, -28.5f)
                                reflectiveQuadTo(480f, 480f)
                                quadToRelative(17f, 0f, 28.5f, 11.5f)
                                reflectiveQuadTo(520f, 520f)
                                quadToRelative(0f, 17f, -11.5f, 28.5f)
                                reflectiveQuadTo(480f, 560f)
                                close()
                                moveToRelative(-188.5f, -11.5f)
                                quadTo(280f, 537f, 280f, 520f)
                                reflectiveQuadToRelative(11.5f, -28.5f)
                                quadTo(303f, 480f, 320f, 480f)
                                reflectiveQuadToRelative(28.5f, 11.5f)
                                quadTo(360f, 503f, 360f, 520f)
                                reflectiveQuadToRelative(-11.5f, 28.5f)
                                quadTo(337f, 560f, 320f, 560f)
                                reflectiveQuadToRelative(-28.5f, -11.5f)
                                close()
                                moveTo(640f, 560f)
                                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                                reflectiveQuadTo(600f, 520f)
                                quadToRelative(0f, -17f, 11.5f, -28.5f)
                                reflectiveQuadTo(640f, 480f)
                                quadToRelative(17f, 0f, 28.5f, 11.5f)
                                reflectiveQuadTo(680f, 520f)
                                quadToRelative(0f, 17f, -11.5f, 28.5f)
                                reflectiveQuadTo(640f, 560f)
                                close()
                                moveTo(480f, 720f)
                                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                                reflectiveQuadTo(440f, 680f)
                                quadToRelative(0f, -17f, 11.5f, -28.5f)
                                reflectiveQuadTo(480f, 640f)
                                quadToRelative(17f, 0f, 28.5f, 11.5f)
                                reflectiveQuadTo(520f, 680f)
                                quadToRelative(0f, 17f, -11.5f, 28.5f)
                                reflectiveQuadTo(480f, 720f)
                                close()
                                moveToRelative(-188.5f, -11.5f)
                                quadTo(280f, 697f, 280f, 680f)
                                reflectiveQuadToRelative(11.5f, -28.5f)
                                quadTo(303f, 640f, 320f, 640f)
                                reflectiveQuadToRelative(28.5f, 11.5f)
                                quadTo(360f, 663f, 360f, 680f)
                                reflectiveQuadToRelative(-11.5f, 28.5f)
                                quadTo(337f, 720f, 320f, 720f)
                                reflectiveQuadToRelative(-28.5f, -11.5f)
                                close()
                                moveTo(640f, 720f)
                                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                                reflectiveQuadTo(600f, 680f)
                                quadToRelative(0f, -17f, 11.5f, -28.5f)
                                reflectiveQuadTo(640f, 640f)
                                quadToRelative(17f, 0f, 28.5f, 11.5f)
                                reflectiveQuadTo(680f, 680f)
                                quadToRelative(0f, 17f, -11.5f, 28.5f)
                                reflectiveQuadTo(640f, 720f)
                                close()
                            }
                        }.build()
                return cachedCalendarMonth!!
            }
        private var cachedCalendarMonth: ImageVector? = null

        val FavoriteBorder: ImageVector
            get() {
                if (cachedFavoriteBorder != null) return cachedFavoriteBorder!!
                cachedFavoriteBorder =
                    ImageVector
                        .Builder(
                            name = "FavoriteBorder",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(480f, 839f)
                                lineToRelative(-41f, -37f)
                                quadToRelative(-105.77f, -97.12f, -174.88f, -167.56f)
                                quadTo(195f, 564f, 154f, 508.5f)
                                reflectiveQuadTo(96.5f, 408f)
                                quadTo(80f, 363f, 80f, 317f)
                                quadToRelative(0f, -90.15f, 60.5f, -150.58f)
                                quadTo(201f, 106f, 290f, 106f)
                                quadToRelative(57f, 0f, 105.5f, 27f)
                                reflectiveQuadToRelative(84.5f, 78f)
                                quadToRelative(42f, -54f, 89f, -79.5f)
                                reflectiveQuadTo(670f, 106f)
                                quadToRelative(89f, 0f, 149.5f, 60.42f)
                                quadTo(880f, 226.85f, 880f, 317f)
                                quadToRelative(0f, 46f, -16.5f, 91f)
                                reflectiveQuadTo(806f, 508.5f)
                                quadTo(765f, 564f, 695.88f, 634.44f)
                                quadTo(626.77f, 704.88f, 521f, 802f)
                                lineToRelative(-41f, 37f)
                                close()
                                moveToRelative(0f, -79f)
                                quadToRelative(101.24f, -93f, 166.62f, -159.5f)
                                quadTo(712f, 534f, 750.5f, 484f)
                                reflectiveQuadToRelative(54f, -89.14f)
                                quadToRelative(15.5f, -39.13f, 15.5f, -77.72f)
                                quadToRelative(0f, -66.14f, -42f, -108.64f)
                                reflectiveQuadTo(670.22f, 166f)
                                quadToRelative(-51.52f, 0f, -95.37f, 31.5f)
                                reflectiveQuadTo(504f, 286f)
                                horizontalLineToRelative(-49f)
                                quadToRelative(-26f, -56f, -69.85f, -88f)
                                quadToRelative(-43.85f, -32f, -95.37f, -32f)
                                quadTo(224f, 166f, 182f, 208.5f)
                                reflectiveQuadToRelative(-42f, 108.82f)
                                quadToRelative(0f, 38.68f, 15.5f, 78.18f)
                                quadToRelative(15.5f, 39.5f, 54f, 90f)
                                reflectiveQuadTo(314f, 602f)
                                quadToRelative(66f, 66f, 166f, 158f)
                                close()
                                moveToRelative(0f, -297f)
                                close()
                            }
                        }.build()
                return cachedFavoriteBorder!!
            }
        private var cachedFavoriteBorder: ImageVector? = null

        val Audiotrack: ImageVector
            get() {
                if (cachedAudiotrack != null) return cachedAudiotrack!!
                cachedAudiotrack =
                    ImageVector
                        .Builder(
                            name = "Audiotrack",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 960f,
                            viewportHeight = 960f,
                        ).apply {
                            path(fill = SolidColor(Color.Black)) {
                                moveTo(286.5f, 796.5f)
                                quadTo(243f, 753f, 243f, 690f)
                                reflectiveQuadToRelative(43.5f, -106.5f)
                                quadTo(330f, 540f, 393f, 540f)
                                quadToRelative(28f, 0f, 50.5f, 8f)
                                reflectiveQuadToRelative(39.5f, 22f)
                                verticalLineToRelative(-450f)
                                horizontalLineToRelative(234f)
                                verticalLineToRelative(135f)
                                horizontalLineTo(543f)
                                verticalLineToRelative(435f)
                                quadToRelative(0f, 63f, -43.5f, 106.5f)
                                reflectiveQuadTo(393f, 840f)
                                quadToRelative(-63f, 0f, -106.5f, -43.5f)
                                close()
                            }
                        }.build()
                return cachedAudiotrack!!
            }
        private var cachedAudiotrack: ImageVector? = null
    }

    /** Directional glyphs that flip in a right-to-left layout. */
    object AutoMirrored {
        object Filled {
            val ArrowBack: ImageVector
                get() {
                    if (cachedArrowBack != null) return cachedArrowBack!!
                    cachedArrowBack =
                        ImageVector
                            .Builder(
                                name = "ArrowBack",
                                defaultWidth = 24.dp,
                                defaultHeight = 24.dp,
                                viewportWidth = 960f,
                                viewportHeight = 960f,
                                autoMirror = true,
                            ).apply {
                                path(fill = SolidColor(Color.Black)) {
                                    moveTo(274f, 510f)
                                    lineToRelative(248f, 248f)
                                    lineToRelative(-42f, 42f)
                                    lineToRelative(-320f, -320f)
                                    lineToRelative(320f, -320f)
                                    lineToRelative(42f, 42f)
                                    lineToRelative(-248f, 248f)
                                    horizontalLineToRelative(526f)
                                    verticalLineToRelative(60f)
                                    horizontalLineTo(274f)
                                    close()
                                }
                            }.build()
                    return cachedArrowBack!!
                }
            private var cachedArrowBack: ImageVector? = null

            val VolumeUp: ImageVector
                get() {
                    if (cachedVolumeUp != null) return cachedVolumeUp!!
                    cachedVolumeUp =
                        ImageVector
                            .Builder(
                                name = "VolumeUp",
                                defaultWidth = 24.dp,
                                defaultHeight = 24.dp,
                                viewportWidth = 960f,
                                viewportHeight = 960f,
                                autoMirror = true,
                            ).apply {
                                path(fill = SolidColor(Color.Black)) {
                                    moveTo(560f, 829f)
                                    verticalLineToRelative(-62f)
                                    quadToRelative(97f, -28f, 158.5f, -107.5f)
                                    reflectiveQuadTo(780f, 479f)
                                    quadToRelative(0f, -101f, -61f, -181f)
                                    reflectiveQuadTo(560f, 191f)
                                    verticalLineToRelative(-62f)
                                    quadToRelative(124f, 28f, 202f, 125.5f)
                                    reflectiveQuadTo(840f, 479f)
                                    quadToRelative(0f, 127f, -78f, 224.5f)
                                    reflectiveQuadTo(560f, 829f)
                                    close()
                                    moveTo(120f, 600f)
                                    verticalLineToRelative(-240f)
                                    horizontalLineToRelative(160f)
                                    lineToRelative(200f, -200f)
                                    verticalLineToRelative(640f)
                                    lineTo(280f, 600f)
                                    horizontalLineTo(120f)
                                    close()
                                    moveToRelative(420f, 48f)
                                    verticalLineToRelative(-337f)
                                    quadToRelative(55f, 17f, 87.5f, 64f)
                                    reflectiveQuadTo(660f, 480f)
                                    quadToRelative(0f, 57f, -33f, 104f)
                                    reflectiveQuadToRelative(-87f, 64f)
                                    close()
                                }
                            }.build()
                    return cachedVolumeUp!!
                }
            private var cachedVolumeUp: ImageVector? = null
        }

        object Outlined {
            val Login: ImageVector
                get() {
                    if (cachedLogin != null) return cachedLogin!!
                    cachedLogin =
                        ImageVector
                            .Builder(
                                name = "Login",
                                defaultWidth = 24.dp,
                                defaultHeight = 24.dp,
                                viewportWidth = 960f,
                                viewportHeight = 960f,
                                autoMirror = true,
                            ).apply {
                                path(fill = SolidColor(Color.Black)) {
                                    moveTo(481f, 840f)
                                    verticalLineToRelative(-60f)
                                    horizontalLineToRelative(299f)
                                    verticalLineToRelative(-600f)
                                    horizontalLineTo(481f)
                                    verticalLineToRelative(-60f)
                                    horizontalLineToRelative(299f)
                                    quadToRelative(24f, 0f, 42f, 18f)
                                    reflectiveQuadToRelative(18f, 42f)
                                    verticalLineToRelative(600f)
                                    quadToRelative(0f, 24f, -18f, 42f)
                                    reflectiveQuadToRelative(-42f, 18f)
                                    horizontalLineTo(481f)
                                    close()
                                    moveToRelative(-55f, -185f)
                                    lineToRelative(-43f, -43f)
                                    lineToRelative(102f, -102f)
                                    horizontalLineTo(120f)
                                    verticalLineToRelative(-60f)
                                    horizontalLineToRelative(363f)
                                    lineTo(381f, 348f)
                                    lineToRelative(43f, -43f)
                                    lineToRelative(176f, 176f)
                                    lineToRelative(-174f, 174f)
                                    close()
                                }
                            }.build()
                    return cachedLogin!!
                }
            private var cachedLogin: ImageVector? = null

            val Logout: ImageVector
                get() {
                    if (cachedLogout != null) return cachedLogout!!
                    cachedLogout =
                        ImageVector
                            .Builder(
                                name = "Logout",
                                defaultWidth = 24.dp,
                                defaultHeight = 24.dp,
                                viewportWidth = 960f,
                                viewportHeight = 960f,
                                autoMirror = true,
                            ).apply {
                                path(fill = SolidColor(Color.Black)) {
                                    moveTo(180f, 840f)
                                    quadToRelative(-24f, 0f, -42f, -18f)
                                    reflectiveQuadToRelative(-18f, -42f)
                                    verticalLineToRelative(-600f)
                                    quadToRelative(0f, -24f, 18f, -42f)
                                    reflectiveQuadToRelative(42f, -18f)
                                    horizontalLineToRelative(299f)
                                    verticalLineToRelative(60f)
                                    horizontalLineTo(180f)
                                    verticalLineToRelative(600f)
                                    horizontalLineToRelative(299f)
                                    verticalLineToRelative(60f)
                                    horizontalLineTo(180f)
                                    close()
                                    moveToRelative(486f, -185f)
                                    lineToRelative(-43f, -43f)
                                    lineToRelative(102f, -102f)
                                    horizontalLineTo(360f)
                                    verticalLineToRelative(-60f)
                                    horizontalLineToRelative(363f)
                                    lineTo(621f, 348f)
                                    lineToRelative(43f, -43f)
                                    lineToRelative(176f, 176f)
                                    lineToRelative(-174f, 174f)
                                    close()
                                }
                            }.build()
                    return cachedLogout!!
                }
            private var cachedLogout: ImageVector? = null
        }
    }
}
