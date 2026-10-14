package com.misgus.agustream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Raw brand palette.
 *
 * It is internal on purpose: it feeds `darkScheme` and the scrims only, while
 * the UI reads every semantic colour from `MaterialTheme.colorScheme` roles. That
 * keeps stock Material3 components and custom surfaces on the same colours.
 */
internal object BrandPalette {
    /**
     * App background. The single source of truth for the near-black the whole
     * UI sits on and the colour every page fade ends on.
     */
    val background = Color(0xFF0B0803)

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
     * Default text colour: white. Headings and body copy share it; secondary
     * roles step down through alpha instead of a separate grey.
     */
    val textPrimary = Color(0xFFFFFFFF)

    /** Metadata, captions and secondary copy: white, dimmed so hierarchy survives. */
    val textSecondary = Color(0xFFFFFFFF).copy(alpha = 0.70f)

    /** Release year / metadata under a poster title. */
    val posterYear = Color(0xFFFFFFFF).copy(alpha = 0.60f)

    /** Idle hero carousel dot: a neutral grey that reads over artwork. */
    val indicator = Color(0xFF9E9E9E)

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

    /** Hero carousel dots over artwork: muted grey when idle, white when active. */
    val heroIndicatorInactive = BrandPalette.indicator
    val heroIndicatorActive = BrandPalette.textPrimary
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
 * Corner radii, as the raw values behind `AppShapes`.
 *
 * Kept small on purpose: the UI reads square-ish, not rounded. A surface that
 * needs a radius in the draw phase (such as `motionBorder`) reads the `Dp` here,
 * while anything that takes a `Shape` uses `MaterialTheme.shapes`.
 */
object AppCorner {
    val extraSmall: Dp = 2.dp
    val small: Dp = 4.dp
    val medium: Dp = 6.dp
    val large: Dp = 8.dp
    val extraLarge: Dp = 12.dp
}

/** Sizes shared by the shell. */
object AppSizes {
    /** Width of the vertical navigation rail used on wide windows. */
    val railWidth: Dp = 80.dp

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

    /** Width of the focus ring drawn around interactive controls for keyboard/D-pad. */
    val focusRing: Dp = 2.dp
}

/** Scale factors shared by interactive surfaces. */
object AppScale {
    /** Scale a poster grows to on hover or focus. */
    const val HOVER = 1.04f
}

/**
 * Sizing for the title artwork (logo) on a billboard.
 *
 * A logo is contained in a box: a fixed optical height plus a [maxWidth] guard,
 * so a square logo cannot tower over the copy while a very wide one cannot
 * overflow its column. Both billboards read these values, so they stay in sync.
 */
object AppLogoSize {
    /** Optical height of the title logo on the wide home billboard. */
    val homeHeight: Dp = 150.dp

    /** Optical height of the title logo on the wide detail billboard. */
    val detailHeight: Dp = 96.dp

    /** Optical height of the title logo on a phone's detail billboard. */
    val compactHeight: Dp = 56.dp

    /** Widest a logo may grow; a wider logo scales down to fit. */
    val maxWidth: Dp = 520.dp
}

/**
 * Layout for a hero synopsis.
 *
 * A hero synopsis is a teaser, not reading copy: it shows [MAX_LINES] lines at
 * most, with the rest behind "More info", and a [maxWidth] shared by both
 * billboards, so the copy column is the same width on Home and Detail. It
 * keeps the measure around 80 characters (45–90 is the legibility range).
 */
object AppSynopsis {
    /**
     * Widest the synopsis grows, capped at the detail billboard's copy column so
     * both billboards wrap identically.
     */
    val maxWidth: Dp = 720.dp

    /** Lines shown on a billboard; the detail screen shows the rest. */
    const val MAX_LINES = 3
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

    /**
     * Flat dim kept over the hero artwork, so it reads darker than the raw
     * image. Applied as the terminal stop of the hero gradient.
     */
    const val HERO_IMAGE_DIM = 0.30f

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

    /** Translucent fill behind the selected top-navigation item. */
    const val NAV_SELECTED = 0.12f
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

    /**
     * Home billboard, left-to-right: fully opaque over the copy column and a
     * flat [AppAlpha.HERO_IMAGE_DIM] over the rest, so the right-aligned artwork
     * stays visible but reads a touch darker.
     */
    val heroHorizontal: Brush =
        Brush.horizontalGradient(
            0f to BrandPalette.background,
            0.25f to BrandPalette.background.copy(alpha = 0.92f),
            0.42f to BrandPalette.background.copy(alpha = 0.6f),
            0.6f to BrandPalette.background.copy(alpha = 0.2f),
            0.78f to BrandPalette.background.copy(alpha = AppAlpha.HERO_IMAGE_DIM),
        )

    /**
     * Wide desktop billboard, left-to-right: opaque over the left column and
     * the copy, then a short fade that dissolves the artwork's left edge into
     * the page background. The fade ends on a flat [AppAlpha.HERO_IMAGE_DIM]
     * over the artwork, so the image reads a touch darker without going black.
     */
    val heroWideLeft: Brush =
        Brush.horizontalGradient(
            0f to BrandPalette.background,
            0.25f to BrandPalette.background,
            0.45f to BrandPalette.background.copy(alpha = AppAlpha.HERO_IMAGE_DIM),
        )

    /**
     * Home billboard, bottom-up: clear over the upper half and a long, soft fade
     * into the page background, so the hero dissolves into the section below
     * without a hard seam. The mid stop keeps the ramp gentle instead of sudden.
     */
    val heroBottom: Brush =
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.45f to Color.Transparent,
            0.78f to BrandPalette.background.copy(alpha = 0.55f),
            1f to BrandPalette.background,
        )

    /** Home billboard top fade, subtle enough to keep the window controls legible. */
    val heroTop: Brush =
        Brush.verticalGradient(
            0f to BrandPalette.background.copy(alpha = 0.45f),
            0.2f to Color.Transparent,
        )

    /** Home billboard right-edge vignette. */
    val heroRightVignette: Brush =
        Brush.horizontalGradient(
            0f to Color.Transparent,
            0.7f to Color.Transparent,
            1f to BrandPalette.background.copy(alpha = 0.25f),
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
    fun title(): TextStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 21.sp)

    /** Year, release info or episode tag under the title. */
    @Composable
    fun meta(): TextStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
}

/**
 * Colours for surfaces that live outside the composition.
 *
 * The native desktop title bar is painted by AWT before Compose draws, so it
 * cannot read `MaterialTheme`; it reads these values instead.
 */
object AppWindowChrome {
    /** Native title-bar colour; matches the page background and the rail. */
    val titleBar: Color = BrandPalette.background

    /** Native window border, matched to the title bar so the frame disappears. */
    val border: Color = BrandPalette.background

    /** Window background behind Compose, so a resize does not flash. */
    val windowBackground: Color = BrandPalette.background

    /** Native title-bar text and caption buttons. */
    val foreground: Color = BrandPalette.textPrimary
}
