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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

/**
 * Raw brand palette.
 *
 * It is private on purpose: it feeds [darkScheme] and the scrims only, while the
 * UI reads every semantic colour from `MaterialTheme.colorScheme` roles. That
 * keeps stock Material3 components and custom surfaces on the same colours.
 */
private object BrandPalette {
    /** App background, near black. */
    val background = Color(0xFF0A0A0C)

    /** Cards, panels and rails. */
    val surface = Color(0xFF151518)

    /** Hover / selected surfaces. */
    val surfaceHigh = Color(0xFF1F1F24)

    /**
     * Attention colour (amber): selection, active states and badges. It is not a
     * call-to-action colour — primary CTAs use the white pill button.
     */
    val accent = Color(0xFFF0A020)
    val onAccent = Color(0xFF1A1200)

    /**
     * Default text colour. It matches the poster captions on purpose: body copy
     * stays muted instead of glaring white.
     */
    val textPrimary = Color(0xFFAFB0B0)
    val textSecondary = Color(0xFF9A9AA4)

    /** Release year / metadata under a poster title. */
    val posterYear = Color(0xFF8A8A92)

    val divider = Color(0xFF26262B)
}

/**
 * Overlay colours that the Material colour scheme has no role for.
 *
 * A scrim over media is not a theme colour, so it lives here rather than in a
 * `colorScheme` slot.
 */
object AppColors {
    /** Dark scrim over media, used behind badges and progress tracks. */
    val mediaScrim = Color.Black.copy(alpha = 0.54f)
}

/** Spacing scale, on the Material 8dp system. */
object AppSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

/**
 * Corner radii, as the raw values behind [AppShapes].
 *
 * They match the Material 3 shape tokens; a surface that needs a radius in the
 * draw phase (such as `motionBorder`) reads the `Dp` here, while anything that
 * takes a `Shape` uses `MaterialTheme.shapes`.
 */
object AppCorner {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 28.dp

    /** The Material 3 button "full" radius, used by the pill button. */
    val full: Dp = 9999.dp
}

/** Material 3 shapes, built from [AppCorner]. */
private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(AppCorner.extraSmall),
        small = RoundedCornerShape(AppCorner.small),
        medium = RoundedCornerShape(AppCorner.medium),
        large = RoundedCornerShape(AppCorner.large),
        extraLarge = RoundedCornerShape(AppCorner.extraLarge),
    )

/** Sizes shared by the shell. */
object AppSizes {
    /** Width of the vertical navigation rail used on wide windows. */
    val railWidth: Dp = 80.dp

    /** Height of the horizontal top navigation bar, used on wide windows. */
    val topBarHeight: Dp = 64.dp

    /** Height of the icon-only bottom navigation bar, used on compact windows. */
    val bottomBarHeight: Dp = 80.dp

    /**
     * Height of a primary control (call-to-action button, action icon box).
     *
     * Material's minimum touch target is 48dp, so the control is at least that
     * tall even though the visual design reads tighter.
     */
    val controlHeight: Dp = 48.dp

    /** Smallest side of an interactive target, per Material's touch guidance. */
    val minTouchTarget: Dp = 48.dp
}

/** Shared icon sizes. */
object AppIconSize {
    val sm: Dp = 18.dp
    val md: Dp = 20.dp
    val lg: Dp = 24.dp
}

/** Reusable overlay alphas, so scrims and shadows stay consistent. */
object AppAlpha {
    /** Top scrim that keeps the detail back button legible over any content. */
    const val DETAIL_TOP_SCRIM = 0.55f

    /** Secondary hero action background. */
    const val HERO_ACTION = 0.18f

    /** Inactive hero page indicator. */
    const val HERO_INDICATOR = 0.35f

    /** Readability scrim over media, from the strong left edge to the soft right edge. */
    const val MEDIA_SCRIM_STRONG = 0.85f
    const val MEDIA_SCRIM_MEDIUM = 0.35f

    /** Strong top edge of a short media header, such as a dialog. */
    const val MEDIA_SCRIM_HEADER = 0.6f

    /** Player top and bottom control scrims. */
    const val PLAYER_SCRIM = 0.8f

    /** Play button backdrop over video. */
    const val PLAYER_BUTTON = 0.45f

    /** Interaction feedback for custom surfaces. */
    const val INTERACT_HOVER = 0.08f
    const val INTERACT_FOCUS = 0.18f
    const val INTERACT_PRESSED = 0.16f
}

/**
 * Prebuilt scrims.
 *
 * Defined once so a gradient can be tuned in a single place and reused by any
 * screen that needs the same readability treatment.
 */
object AppScrim {
    /** Left-to-right scrim that keeps copy readable over bright artwork. */
    val mediaHorizontal: Brush =
        Brush.horizontalGradient(
            0f to Color.Black.copy(alpha = AppAlpha.MEDIA_SCRIM_STRONG),
            0.45f to Color.Black.copy(alpha = AppAlpha.MEDIA_SCRIM_MEDIUM),
            0.8f to Color.Transparent,
        )

    /** Bottom fade to the page background, dark enough for copy to sit on any artwork. */
    val mediaBottom: Brush =
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.4f to Color.Black.copy(alpha = AppAlpha.MEDIA_SCRIM_MEDIUM),
            1f to BrandPalette.background,
        )

    /** Stronger fade for short media headers, such as dialogs. */
    val dialogHeader: Brush =
        Brush.verticalGradient(
            0f to Color.Black.copy(alpha = AppAlpha.MEDIA_SCRIM_HEADER),
            0.45f to Color.Transparent,
            1f to BrandPalette.background,
        )

    /**
     * Dark base behind a full-screen hero.
     *
     * A slow or missing image never leaves a bright gap: whatever the artwork
     * does not cover falls back to this near-black gradient.
     */
    val detailBackdrop: Brush =
        Brush.verticalGradient(
            0f to BrandPalette.surfaceHigh,
            0.55f to BrandPalette.surface,
            1f to BrandPalette.background,
        )

    /**
     * Fade along the top of a screen, so a fixed control such as a back button
     * always sits on a dark strip instead of on bright artwork or content.
     */
    val detailTop: Brush =
        Brush.verticalGradient(
            0f to Color.Black.copy(alpha = AppAlpha.DETAIL_TOP_SCRIM),
            1f to Color.Transparent,
        )

    /** Top control scrim over the player. */
    val playerTop: Brush =
        Brush.verticalGradient(
            0f to Color.Black.copy(alpha = AppAlpha.PLAYER_SCRIM),
            1f to Color.Transparent,
        )

    /** Bottom control scrim over the player. */
    val playerBottom: Brush =
        Brush.verticalGradient(
            0f to Color.Transparent,
            1f to Color.Black.copy(alpha = AppAlpha.PLAYER_SCRIM),
        )
}

/**
 * Caption styles for the title and secondary metadata shown under a poster.
 *
 * These are Material type roles, so every poster in the app — library grid, home
 * rows, catalog grids and "more like this" — shares the same size and weight
 * hierarchy. Colour is applied at the call site from the colour scheme.
 */
object PosterCaption {
    /** Movie / series title under the artwork. */
    @Composable
    fun title(): TextStyle = MaterialTheme.typography.titleSmall

    /** Year, release info or episode tag under the title. */
    @Composable
    fun meta(): TextStyle = MaterialTheme.typography.bodySmall
}

/**
 * Colours for surfaces that live outside the composition.
 *
 * The native desktop title bar is painted by AWT before Compose draws, so it
 * cannot read `MaterialTheme`; it reads these values instead.
 */
object AppWindowChrome {
    val background: Color = BrandPalette.background
    val foreground: Color = BrandPalette.textPrimary
}

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
        secondary = Color(0xFFD3C4AC),
        onSecondary = Color(0xFF382E1C),
        secondaryContainer = Color(0xFF4F422A),
        onSecondaryContainer = Color(0xFFF0E0C4),
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
 * Applies the Inter typeface to every Material3 typography slot.
 *
 * Building the full scale (instead of only a handful of slots) keeps components
 * that reach for other roles — dialogs, text fields, chips — on the same
 * typeface and weight hierarchy. No slot carries a colour: text colour comes
 * from the colour scheme at the call site, exactly as Material intends.
 */
private fun interTypography(inter: FontFamily): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displayMedium = base.displayMedium.copy(fontFamily = inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displaySmall = base.displaySmall.copy(fontFamily = inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = inter, fontWeight = FontWeight.Normal),
        bodyMedium = base.bodyMedium.copy(fontFamily = inter, fontWeight = FontWeight.Normal),
        bodySmall = base.bodySmall.copy(fontFamily = inter, fontWeight = FontWeight.Normal),
        labelLarge = base.labelLarge.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
    )
}

/** The app's dark Material3 theme, with the Inter typeface and the MD3 shapes. */
@Composable
fun AgustreamTheme(content: @Composable () -> Unit) {
    val inter =
        FontFamily(
            Font(Res.font.inter_regular, FontWeight.Normal),
            Font(Res.font.inter_medium, FontWeight.Medium),
            Font(Res.font.inter_semibold, FontWeight.SemiBold),
            Font(Res.font.inter_bold, FontWeight.Bold),
        )

    MaterialTheme(
        colorScheme = darkScheme,
        typography = interTypography(inter),
        shapes = AppShapes,
        content = content,
    )
}
