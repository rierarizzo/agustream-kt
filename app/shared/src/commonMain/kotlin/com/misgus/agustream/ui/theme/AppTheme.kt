package com.misgus.agustream.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.ibm_plex_sans_bold
import agustream.app.shared.generated.resources.ibm_plex_sans_medium
import agustream.app.shared.generated.resources.ibm_plex_sans_regular
import agustream.app.shared.generated.resources.ibm_plex_sans_semibold

/** Design tokens taken from the AIOStreams reference UI. */
object AppColors {
    /** App background, near black. */
    val background = Color(0xFF0A0A0C)

    /** Cards, panels and rails. */
    val surface = Color(0xFF151518)

    /** Hover / selected surfaces. */
    val surfaceHigh = Color(0xFF1F1F24)

    /** Brand accent: amber, used for focus, toggles and badges. */
    val accent = Color(0xFFF0A020)
    val onAccent = Color(0xFF1A1200)

    val textPrimary = Color(0xFFF5F5F7)
    val textSecondary = Color(0xFF9A9AA4)

    /** Movie title under a poster. */
    val posterTitle = Color(0xFFAFB0B0)

    /** Release year under a poster. */
    val posterYear = Color(0xFF62605D)

    val divider = Color(0xFF26262B)
}

/** Spacing scale. */
object AppSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

/** Corner radii. */
object AppRadii {
    val sm: Dp = 6.dp
    val md: Dp = 10.dp
    val lg: Dp = 16.dp

    val smShape = RoundedCornerShape(sm)
    val mdShape = RoundedCornerShape(md)
    val lgShape = RoundedCornerShape(lg)
}

/** Sizes shared by the shell. */
object AppSizes {
    /** Width of the icon-only navigation rail. */
    val sideRailWidth: Dp = 80.dp

    /** Size of a rail button. */
    val railButton: Dp = 48.dp
}

/**
 * Text styles for the title and year shown under poster cards.
 *
 * Derived from the theme's text styles (so the typeface and weight are kept)
 * with a slightly smaller size and a tighter line height.
 */
object PosterCaption {
    fun title(base: TextStyle): TextStyle =
        base.copy(fontSize = 15.sp, lineHeight = 18.sp, color = AppColors.posterTitle)

    fun year(base: TextStyle): TextStyle =
        base.copy(fontSize = 13.sp, lineHeight = 15.6.sp, color = AppColors.posterYear)
}

private val darkScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.onAccent,
    secondary = AppColors.accent,
    onSecondary = AppColors.onAccent,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.surfaceHigh,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.divider,
)

/** The app's dark Material3 theme, with the IBM Plex Sans typeface. */
@Composable
fun AgustreamTheme(content: @Composable () -> Unit) {
    val plex = FontFamily(
        Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal),
        Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium),
        Font(Res.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.ibm_plex_sans_bold, FontWeight.Bold),
    )

    val base = Typography()
    val typography = base.copy(
        displaySmall = base.displaySmall.copy(fontFamily = plex, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = plex, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = plex, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = plex, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = plex, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = plex, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = plex, fontWeight = FontWeight.Medium, color = AppColors.textPrimary),
        bodyMedium = base.bodyMedium.copy(fontFamily = plex, fontWeight = FontWeight.Medium, color = AppColors.textPrimary),
        bodySmall = base.bodySmall.copy(fontFamily = plex, fontWeight = FontWeight.Medium, color = AppColors.textSecondary),
        labelLarge = base.labelLarge.copy(fontFamily = plex, fontWeight = FontWeight.SemiBold),
    )

    MaterialTheme(
        colorScheme = darkScheme,
        typography = typography,
        content = content,
    )
}
