package com.misgus.agustream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
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

    /** Title of a full-screen state: onboarding, profile picker, splash. */
    @Composable
    fun screenTitle(): TextStyle =
        if (LocalWindowSize.current == WindowSize.COMPACT) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.headlineLarge
        }

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
}
