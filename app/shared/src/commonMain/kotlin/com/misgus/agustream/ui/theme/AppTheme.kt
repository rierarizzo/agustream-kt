package com.misgus.agustream.ui.theme

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.inter_bold
import agustream.app.shared.generated.resources.inter_medium
import agustream.app.shared.generated.resources.inter_regular
import agustream.app.shared.generated.resources.inter_semibold
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

/** Design tokens taken from the AIOStreams reference UI. */
object AppColors {
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
     * Default text colour. It matches the poster captions ([posterTitle]) on
     * purpose: body copy stays muted instead of glaring white.
     */
    val textPrimary = Color(0xFFAFB0B0)
    val textSecondary = Color(0xFF9A9AA4)

    /** Movie title under a poster. */
    val posterTitle = Color(0xFFAFB0B0)

    /** Release year / metadata under a poster title. */
    val posterYear = Color(0xFF8A8A92)

    /** Dark scrim over media, used behind badges and progress tracks. */
    val mediaScrim = Color.Black.copy(alpha = 0.54f)

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

/** Corner radii. Deliberately small: the UI reads square, not rounded. */
object AppRadii {
    val sm: Dp = 2.dp
    val md: Dp = 4.dp
    val lg: Dp = 6.dp
    val xl: Dp = 8.dp

    val smShape = RoundedCornerShape(sm)
    val mdShape = RoundedCornerShape(md)
    val lgShape = RoundedCornerShape(lg)
    val xlShape = RoundedCornerShape(xl)
}

/** Sizes shared by the shell. */
object AppSizes {
    /** Height of the horizontal top navigation bar. */
    val topBarHeight: Dp = 64.dp

    /** Height of the icon-only bottom navigation bar, used on compact windows. */
    val bottomBarHeight: Dp = 64.dp

    /** Height of a primary control (call-to-action button, action icon box). */
    val controlHeight: Dp = 44.dp

    /** Height of the shadow the top bar casts onto the content below. */
    val topBarShadow: Dp = 14.dp
}

/** Shared icon sizes. */
object AppIconSize {
    val sm: Dp = 18.dp
    val md: Dp = 20.dp
    val lg: Dp = 24.dp
}

/** Elevation of floating surfaces. */
object AppElevation {
    /** Home hero billboard. */
    val hero: Dp = 28.dp
}

/** Reusable overlay alphas, so scrims and shadows stay consistent. */
object AppAlpha {
    /** Shadow the top bar casts over the content. */
    const val TOP_BAR_SHADOW = 0.35f

    /** Subtle rim light around the hero card. */
    const val HERO_RIM = 0.08f

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

    /** Translucent highlight for the selected top-navigation item. */
    const val NAV_SELECTED = 0.16f

    /** Play button backdrop over video. */
    const val PLAYER_BUTTON = 0.45f

    /** Player top and bottom control scrims. */
    const val PLAYER_SCRIM = 0.8f

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
            1f to AppColors.background,
        )

    /** Stronger fade for short media headers, such as dialogs. */
    val dialogHeader: Brush =
        Brush.verticalGradient(
            0f to Color.Black.copy(alpha = AppAlpha.MEDIA_SCRIM_HEADER),
            0.45f to Color.Transparent,
            1f to AppColors.background,
        )

    /**
     * Dark base behind a full-screen hero.
     *
     * A slow or missing image never leaves a bright gap: whatever the artwork
     * does not cover falls back to this near-black gradient.
     */
    val detailBackdrop: Brush =
        Brush.verticalGradient(
            0f to AppColors.surfaceHigh,
            0.55f to AppColors.surface,
            1f to AppColors.background,
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
 * Centralised so every poster in the app — library grid, home rows, catalog
 * grids and "more like this" — renders its labels with the same size, weight
 * and colour.
 */
object PosterCaption {
    /** Movie / series title under the artwork. */
    @Composable
    fun title(): TextStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.posterTitle,
        )

    /** Year, release info or episode tag under the title. */
    @Composable
    fun meta(): TextStyle =
        MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = AppColors.posterYear,
        )
}

private val darkScheme =
    darkColorScheme(
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

/**
 * Applies the Inter typeface to every Material3 typography slot.
 *
 * Building the full scale (instead of only a handful of slots) keeps components
 * that reach for other roles — dialogs, text fields, chips — on the same
 * typeface and weight hierarchy.
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
        bodyLarge = base.bodyLarge.copy(fontFamily = inter, fontWeight = FontWeight.Normal, color = AppColors.textPrimary),
        bodyMedium = base.bodyMedium.copy(fontFamily = inter, fontWeight = FontWeight.Normal, color = AppColors.textPrimary),
        bodySmall = base.bodySmall.copy(fontFamily = inter, fontWeight = FontWeight.Normal, color = AppColors.textSecondary),
        labelLarge = base.labelLarge.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(fontFamily = inter, fontWeight = FontWeight.SemiBold),
    )
}

/** The app's dark Material3 theme, with the Inter typeface. */
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
        content = content,
    )
}
