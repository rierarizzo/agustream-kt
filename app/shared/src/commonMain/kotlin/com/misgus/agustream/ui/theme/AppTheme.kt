package com.misgus.agustream.ui.theme

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.inter_bold
import agustream.app.shared.generated.resources.inter_medium
import agustream.app.shared.generated.resources.inter_regular
import agustream.app.shared.generated.resources.inter_semibold
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.misgus.agustream.fonts.generated.resources.ibm_plex_sans_bold
import com.misgus.agustream.fonts.generated.resources.ibm_plex_sans_medium
import com.misgus.agustream.fonts.generated.resources.ibm_plex_sans_regular
import com.misgus.agustream.fonts.generated.resources.ibm_plex_sans_semibold
import org.jetbrains.compose.resources.Font
import com.misgus.agustream.fonts.generated.resources.Res as FontsRes

/** Material 3 shapes, built from [AppCorner]. */
private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(AppCorner.extraSmall),
        small = RoundedCornerShape(AppCorner.small),
        medium = RoundedCornerShape(AppCorner.medium),
        large = RoundedCornerShape(AppCorner.large),
        extraLarge = RoundedCornerShape(AppCorner.extraLarge),
    )

/**
 * The app's Material 3 colour scheme.
 *
 * Every role is filled so stock Material3 components (text fields, switches,
 * chips, dialogs, snackbars) render on the same palette as the custom surfaces.
 */
private val darkScheme =
    darkColorScheme(
        primary = BrandPalette.accent,
        onPrimary = BrandPalette.onAccent,
        primaryContainer = Color(0xFF4A3208),
        onPrimaryContainer = Color(0xFFFFDDAD),
        inversePrimary = Color(0xFF7A5900),
        secondary = Color(0xFFCBC9CE),
        onSecondary = Color(0xFF323034),
        secondaryContainer = Color(0xFF47464A),
        onSecondaryContainer = Color(0xFFE6E4E9),
        tertiary = Color(0xFF8FCBD2),
        onTertiary = Color(0xFF00363D),
        tertiaryContainer = Color(0xFF004F58),
        onTertiaryContainer = Color(0xFFAAE7EF),
        background = BrandPalette.background,
        onBackground = BrandPalette.textPrimary,
        surface = BrandPalette.surface,
        onSurface = BrandPalette.textPrimary,
        surfaceVariant = BrandPalette.surfaceHigh,
        onSurfaceVariant = BrandPalette.textSecondary,
        surfaceTint = BrandPalette.accent,
        inverseSurface = Color(0xFFE6E0E9),
        inverseOnSurface = Color(0xFF313033),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF5A5A63),
        outlineVariant = BrandPalette.divider,
        scrim = Color.Black,
        surfaceBright = Color(0xFF35353B),
        surfaceDim = BrandPalette.background,
        surfaceContainerLowest = BrandPalette.background,
        surfaceContainerLow = Color(0xFF111114),
        surfaceContainer = BrandPalette.surface,
        surfaceContainerHigh = BrandPalette.surfaceHigh,
        surfaceContainerHighest = Color(0xFF2A2A30),
    )

/**
 * Applies [font] to every Material3 typography slot.
 *
 * Building the full scale (instead of only a handful of slots) keeps components
 * that reach for other roles — dialogs, text fields, chips — on the same
 * typeface and weight hierarchy. No slot carries a colour: text colour comes
 * from the colour scheme at the call site, exactly as Material intends.
 */
private fun appTypography(font: FontFamily): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = font, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
        displayMedium = base.displayMedium.copy(fontFamily = font, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
        displaySmall = base.displaySmall.copy(fontFamily = font, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = font, fontWeight = FontWeight.Medium, letterSpacing = (-0.25).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = font, fontWeight = FontWeight.Medium, letterSpacing = (-0.25).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        titleLarge = base.titleLarge.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        titleMedium = base.titleMedium.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        titleSmall = base.titleSmall.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        bodyLarge = base.bodyLarge.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        bodyMedium = base.bodyMedium.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        bodySmall = base.bodySmall.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        labelLarge = base.labelLarge.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        labelMedium = base.labelMedium.copy(fontFamily = font, fontWeight = FontWeight.Normal),
        labelSmall = base.labelSmall.copy(fontFamily = font, fontWeight = FontWeight.Normal),
    )
}

/** Typeface in use. Set to `true` to compare against IBM Plex Sans. */
private const val USE_IBM_PLEX_SANS = false

@Composable
private fun interFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold),
    )

@Composable
private fun ibmPlexSansFontFamily(): FontFamily =
    FontFamily(
        Font(FontsRes.font.ibm_plex_sans_regular, FontWeight.Normal),
        Font(FontsRes.font.ibm_plex_sans_medium, FontWeight.Medium),
        Font(FontsRes.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
        Font(FontsRes.font.ibm_plex_sans_bold, FontWeight.Bold),
    )

/** The app's dark Material3 theme, with the Inter typeface and the MD3 shapes. */
@Composable
fun AgustreamTheme(content: @Composable () -> Unit) {
    val fontFamily = if (USE_IBM_PLEX_SANS) ibmPlexSansFontFamily() else interFontFamily()

    MaterialTheme(
        colorScheme = darkScheme,
        typography = appTypography(fontFamily),
        shapes = AppShapes,
        content = content,
    )
}
