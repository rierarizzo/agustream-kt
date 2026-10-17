package com.misgus.pengu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type roles that step down one size on a compact window.
 *
 * Material's `display*` roles and the largest `headline` sizes are sized for a
 * big screen; on a phone they tower over the layout, so the next role down is
 * used there. Desktop and tablet keep the full scale, so nothing changes for
 * them.
 */
object AppType {
    /** Title of a hero (a movie or series name). */
    @Composable
    fun heroTitle(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.headlineLarge
        } else {
            MaterialTheme.typography.displayMedium
        }

    /** Title of a screen: onboarding, profile picker, splash, catalog, settings. */
    @Composable
    fun screenTitle(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.headlineLarge
        }

    /** Subtitle next to a section title (e.g. the "Movies"/"Series" tag). */
    @Composable
    fun sectionSubtitle(): TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)

    /**
     * Hero synopsis and other long copy over artwork.
     *
     * Both billboards share this style, so the description reads at the same
     * weight and size on Home and Detail; the measure is shared by
     * `HeroSynopsis` too. It sits one point under `bodyLarge` (17sp / 15sp on a
     * phone), with a Normal weight, so the teaser reads lighter than body copy.
     */
    @Composable
    fun heroBody(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 23.sp)
        } else {
            MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp)
        }

    /**
     * Metadata line under a hero title (`Series • 2026 • Drama • ★ 7.8`).
     *
     * Reads one step down the label roles, like the rest of the billboard copy,
     * but in Inter Tight: the line is a dense ribbon, and the tighter fit keeps
     * it to one line under the title on both the home and the detail billboard.
     */
    @Composable
    fun heroMeta(): TextStyle {
        val base =
            if (LocalWindowSize.current == WindowSize.COMPACT) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelLarge
            }
        return base.copy(fontFamily = interTightFontFamily())
    }

    /**
     * Label of a button.
     *
     * Material's `labelLarge` is Medium by default, but the app pins every
     * typography slot to Normal; a button is an action, so it opts back into a
     * heavier weight here instead of at the call site.
     */
    @Composable
    fun buttonLabel(): TextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)

    /**
     * Label of a primary call to action (the filled and amber buttons).
     *
     * One size up and still SemiBold, so "Stream Now" carries the billboard
     * without shouting like a display heading.
     */
    @Composable
    fun ctaLabel(): TextStyle =
        MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
}
