package com.misgus.pengu.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.pengu.ui.icons.AppIcons
import com.misgus.pengu.ui.theme.AppSizes
import com.misgus.pengu.ui.theme.AppSpacing
import com.misgus.pengu.ui.theme.AppWindowChrome

/**
 * The custom title bar drawn by Compose.
 *
 * The window keeps its native frame; the native procedure erases the non-client
 * area and asks this bar's measured geometry which part the pointer is over
 * (see `WindowsWindowFrameProc`). So the empty area drags/maximizes/snaps like a
 * real caption and the buttons answer `HTMINBUTTON`/`HTMAXBUTTON`/`HTCLOSE`,
 * while the bar itself is ordinary Compose content.
 */
@Composable
internal fun WindowsWindowTitleBar(
    state: WindowsWindowFrameState,
    title: String,
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The bar leaves the composition in fullscreen; clear its geometry so the
    // procedure stops reserving the top strip as caption.
    DisposableEffect(state) {
        onDispose { state.reset() }
    }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(AppSizes.titleBarHeight)
                .background(AppWindowChrome.titleBar)
                .onSizeChanged { state.titleBarHeightPx = it.height },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = AppWindowChrome.foreground,
            modifier = Modifier.padding(start = AppSpacing.md),
        )
        Spacer(Modifier.weight(1f))
        CaptionButton(
            icon = AppIcons.Filled.Minimize,
            contentDescription = "Minimize",
            onClick = onMinimize,
            iconSize = minimizeIconSize,
            modifier = Modifier.onGloballyPositioned { state.minimizeRect = it.boundsInWindow() },
        )
        CaptionButton(
            icon = if (isMaximized) AppIcons.Filled.Restore else AppIcons.Filled.Maximize,
            contentDescription = if (isMaximized) "Restore" else "Maximize",
            onClick = onToggleMaximize,
            iconSize = if (isMaximized) restoreIconSize else maximizeIconSize,
            modifier = Modifier.onGloballyPositioned { state.maximizeRect = it.boundsInWindow() },
        )
        CaptionButton(
            icon = AppIcons.Filled.Close,
            contentDescription = "Close",
            onClick = onClose,
            iconSize = closeIconSize,
            close = true,
            modifier = Modifier.onGloballyPositioned { state.closeRect = it.boundsInWindow() },
        )
    }
}

/**
 * One Windows-style caption button.
 *
 * [close] paints the destructive red on hover/press; every other button uses the
 * shared hover surface.
 */
@Composable
private fun CaptionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    close: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()
    val active = hovered || pressed
    val background =
        when {
            close && active -> AppWindowChrome.captionClose
            active -> AppWindowChrome.captionHover
            else -> Color.Transparent
        }
    val tint = if (close && active) AppWindowChrome.captionCloseForeground else AppWindowChrome.foreground
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .width(AppSizes.captionButtonWidth)
                .background(background)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * Caption glyph sizes, tuned per glyph so the set reads even.
 *
 * `Icon` scales the 24-unit viewport, not the drawn content, and the Material
 * caption glyphs fill very different boxes in it: `remove` 14×2, `crop_square`
 * 16×16, `filter_none` 22×22 and `close` ~12.6×12.6. At one shared size the
 * restore reads oversized while minimize/close read undersized. Each is sized so
 * its drawn extents land around 10dp (restore on its front square, which should
 * match maximize).
 */
private val minimizeIconSize: Dp = 17.dp
private val maximizeIconSize: Dp = 16.dp
private val restoreIconSize: Dp = 14.dp
private val closeIconSize: Dp = 18.dp
