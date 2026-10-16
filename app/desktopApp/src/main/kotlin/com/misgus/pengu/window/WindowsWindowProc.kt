@file:Suppress("ktlint:standard:function-naming")

package com.misgus.pengu.window

import androidx.compose.ui.awt.ComposeWindow
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions
import org.jetbrains.skiko.SkiaLayer
import java.awt.Container
import javax.swing.JComponent

/**
 * Draws the custom window frame over the native one.
 *
 * The window keeps its native styles (`WS_CAPTION`, `WS_THICKFRAME`), so the OS
 * keeps owning snap, maximize, resize and the DWM frame. This procedure only
 * erases the non-client area (`WM_NCCALCSIZE`) and tells the OS which part of the
 * window the pointer is over (`WM_NCHITTEST`), so the Compose title bar behaves
 * like a real caption while the rest of the window stays ordinary client area.
 *
 * This follows Animeko's implementation (the reference Compose Desktop app),
 * trimmed to the pieces Pengu needs: no touch/pen bridge, no Windows 10 glass and
 * no per-node Compose hit test — the frame measures its own geometry instead.
 */
internal class WindowsWindowFrameProc(
    window: ComposeWindow,
    private val frameState: WindowsWindowFrameState,
) : WindowProc,
    AutoCloseable {
    private val windowHandle: HWND = HWND(Pointer(window.windowHandle))
    private val defaultProc: Pointer =
        requireNotNull(
            user32.SetWindowLongPtr(windowHandle, WinUser.GWL_WNDPROC, CallbackReference.getFunctionPointer(this)),
        ) { "SetWindowLongPtr failed for the top-level window" }

    // The Compose content lives in a child window, so the hit test must run there
    // (Windows asks the window under the cursor) and the non-client mouse messages
    // must be mirrored back into it for the Compose buttons to render and click.
    private val skiaProc: SkiaLayerWindowProc? =
        window.findSkiaLayer()?.let { SkiaLayerWindowProc(it, ::hitTest) }

    /** Whether the Compose content window was found and hooked. */
    val isChildHooked: Boolean get() = skiaProc != null

    private var hitResult: WinHitResult = WinHitResult.CLIENT
    private var isMaximized: Boolean = windowHandle.isMaximized()
    private var dpi = 0
    private var width = 0
    private var height = 0
    private var frameX = 0
    private var frameY = 0
    private var edgeX = 0
    private var edgeY = 0
    private var padding = 0

    override fun callback(
        hwnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT =
        when (uMsg) {
            WM_NCCALCSIZE -> onNcCalcSize(hwnd, wParam, lParam)
            WM_NCHITTEST -> onNcHitTest(lParam)
            else -> callDefault(hwnd, uMsg, wParam, lParam)
        }

    /**
     * Removes the OS-drawn non-client area while keeping the frame styles.
     *
     * Returning `0` makes the whole window client area, so the Compose content is
     * edge-to-edge; the client rect is only shrunk by the frame thickness so a
     * maximized window does not bleed past the monitor edge.
     */
    private fun onNcCalcSize(
        hwnd: HWND,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT {
        if (wParam.toInt() == 0) return callDefault(hwnd, WM_NCCALCSIZE, wParam, lParam)
        val style = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
        if (style and (WinUser.WS_CAPTION or WinUser.WS_THICKFRAME) == 0) {
            // Fullscreen: no frame left to measure.
            dpi = 0
            frameX = 0
            frameY = 0
            edgeX = 0
            edgeY = 0
            padding = 0
            isMaximized = hwnd.isMaximized()
            return LRESULT(0)
        }
        updateFrameMetrics(hwnd)
        isMaximized = hwnd.isMaximized()
        val params = Structure.newInstance(NcCalcSizeParams::class.java, Pointer(lParam.toLong()))
        params.read()
        params.rgrc[0].apply {
            left += if (isMaximized) frameX + padding else edgeX
            right -= if (isMaximized) frameX + padding else edgeX
            top += if (isMaximized) padding + frameX else 0
            bottom -= if (isMaximized) padding + frameX else edgeY
        }
        params.write()
        return LRESULT(0)
    }

    /**
     * Answers `WM_NCHITTEST` from the last Compose hit test.
     *
     * Windows sends the message to the child window first; that call updates
     * [hitResult], so when the child falls through with `HTTRANSPARENT` this
     * returns the real part (caption, border or client).
     */
    private fun onNcHitTest(lParam: LPARAM): LRESULT {
        hitResult = computeHit(lParam)
        return LRESULT(hitResult.value.toLong())
    }

    /** Resolves the pointer to a caption button, a border, the caption or client. */
    private fun hitTest(lParam: LPARAM): WinHitResult {
        hitResult = computeHit(lParam)
        return hitResult
    }

    /**
     * Caption buttons win over the resize border, so the top strip over the
     * minimize/maximize/close buttons still lights them up (and shows Snap
     * Layouts on maximize) instead of resizing the window.
     */
    private fun computeHit(lParam: LPARAM): WinHitResult {
        val point = lParam.toClientPoint() ?: return WinHitResult.CLIENT
        val frame = frameState.hitTest(point.x, point.y)
        if (frame.isCaptionButton()) return frame
        if (!isMaximized) {
            val border = hitTestBorder(point.x, point.y)
            if (border.isBorder()) return border
        }
        return frame
    }

    private fun hitTestBorder(
        x: Int,
        y: Int,
    ): WinHitResult {
        updateWindowInfo()
        val style = user32.GetWindowLong(windowHandle, WinUser.GWL_STYLE)
        if (style and WinUser.WS_CAPTION == 0) return WinHitResult.NOWHERE
        return when {
            x <= frameX && y > frameY && y < height - frameY -> WinHitResult.BORDER_LEFT
            x <= frameX && y <= frameY -> WinHitResult.BORDER_TOP_LEFT
            x <= frameX -> WinHitResult.BORDER_BOTTOM_LEFT
            y <= frameY && x > frameX && x < width - frameX -> WinHitResult.BORDER_TOP
            y <= frameY -> WinHitResult.BORDER_TOP_RIGHT
            x >= width - frameX && y > frameY && y < height - frameY -> WinHitResult.BORDER_RIGHT
            x >= width - frameX -> WinHitResult.BORDER_BOTTOM_RIGHT
            y >= height - frameY && x > frameX && x < width - frameX -> WinHitResult.BORDER_BOTTOM
            y >= height - frameY -> WinHitResult.BORDER_BOTTOM_RIGHT
            else -> WinHitResult.NOWHERE
        }
    }

    private fun callDefault(
        hwnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT = user32.CallWindowProc(defaultProc, hwnd, uMsg, wParam, lParam)

    private fun updateFrameMetrics(hwnd: HWND) {
        dpi = user32.GetDpiForWindow(hwnd)
        frameX = user32.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
        frameY = user32.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
        edgeX = user32.GetSystemMetricsForDpi(WinUser.SM_CXEDGE, dpi)
        edgeY = user32.GetSystemMetricsForDpi(WinUser.SM_CYEDGE, dpi)
        padding = user32.GetSystemMetricsForDpi(WinUser.SM_CXPADDEDBORDER, dpi)
    }

    private fun updateWindowInfo() {
        updateFrameMetrics(windowHandle)
        val rect = RECT()
        if (user32.GetWindowRect(windowHandle, rect)) {
            rect.read()
            width = rect.right - rect.left
            height = rect.bottom - rect.top
        }
        rect.clear()
    }

    private fun LPARAM.toClientPoint(): POINT? {
        val value = toInt()
        val x = (value and 0xFFFF).toShort().toInt()
        val y = ((value shr 16) and 0xFFFF).toShort().toInt()
        val point = POINT(x, y)
        if (!user32.ScreenToClient(windowHandle, point)) return null
        point.read()
        return point
    }

    override fun close() {
        skiaProc?.close()
        user32.SetWindowLongPtr(windowHandle, WinUser.GWL_WNDPROC, defaultProc)
    }

    /** `NCCALCSIZE_PARAMS`: the suggested client rects and the `WINDOWPOS`. */
    @Structure.FieldOrder("rgrc", "lppos")
    internal class NcCalcSizeParams(
        @JvmField var rgrc: Array<RECT> = Array(3) { RECT() },
        @JvmField var lppos: Pointer? = null,
    ) : Structure()
}

/**
 * Window procedure for the Skia child window, mirroring Animeko.
 *
 * The actual Compose content is a child HWND, so Windows asks it for the hit
 * test. It answers the caption buttons itself and falls through to the parent for
 * everything else (`HTTRANSPARENT`), so the parent owns the caption drag and the
 * resize borders. The non-client mouse messages are re-sent as client ones so the
 * Compose buttons animate and handle the click instead of the OS caption.
 */
private class SkiaLayerWindowProc(
    skiaLayer: SkiaLayer,
    private val hitTest: (LPARAM) -> WinHitResult,
) : WindowProc,
    AutoCloseable {
    private val contentHandle: HWND = HWND(Native.getComponentPointer(skiaLayer.canvas))
    private val defaultProc: Pointer =
        requireNotNull(
            user32.SetWindowLongPtr(contentHandle, WinUser.GWL_WNDPROC, CallbackReference.getFunctionPointer(this)),
        ) { "SetWindowLongPtr failed for the Skia content window" }

    override fun callback(
        hwnd: HWND,
        uMsg: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT =
        when (uMsg) {
            WM_NCHITTEST -> LRESULT(hitTest(lParam).toSkiaHitResult().value.toLong())
            WM_NCMOUSEMOVE -> forward(WM_MOUSEMOVE, wParam, lParam)
            WM_NCLBUTTONDOWN -> forward(WM_LBUTTONDOWN, wParam, lParam)
            WM_NCLBUTTONUP -> forward(WM_LBUTTONUP, wParam, lParam)
            else -> user32.CallWindowProc(defaultProc, hwnd, uMsg, wParam, lParam)
        }

    private fun forward(
        message: Int,
        wParam: WPARAM,
        lParam: LPARAM,
    ): LRESULT {
        user32.SendMessage(contentHandle, message, wParam, lParam)
        return LRESULT(0)
    }

    override fun close() {
        user32.SetWindowLongPtr(contentHandle, WinUser.GWL_WNDPROC, defaultProc)
    }
}

/**
 * The child window only keeps what Windows needs the content to answer. Anything
 * else becomes `HTTRANSPARENT`, so the parent answers the caption and the edges.
 */
private fun WinHitResult.toSkiaHitResult(): WinHitResult =
    when (this) {
        WinHitResult.CLIENT, WinHitResult.CAPTION_MIN, WinHitResult.CAPTION_MAX, WinHitResult.CAPTION_CLOSE -> this
        else -> WinHitResult.TRANSPARENT
    }

private fun WinHitResult.isCaptionButton(): Boolean =
    when (this) {
        WinHitResult.CAPTION_MIN, WinHitResult.CAPTION_MAX, WinHitResult.CAPTION_CLOSE -> true
        else -> false
    }

private fun WinHitResult.isBorder(): Boolean =
    when (this) {
        WinHitResult.BORDER_LEFT,
        WinHitResult.BORDER_RIGHT,
        WinHitResult.BORDER_TOP,
        WinHitResult.BORDER_TOP_LEFT,
        WinHitResult.BORDER_TOP_RIGHT,
        WinHitResult.BORDER_BOTTOM,
        WinHitResult.BORDER_BOTTOM_LEFT,
        WinHitResult.BORDER_BOTTOM_RIGHT,
        -> true

        else -> false
    }

private fun HWND.isMaximized(): Boolean {
    val placement = WinUser.WINDOWPLACEMENT()
    val ok = user32.GetWindowPlacement(this, placement).booleanValue()
    if (ok) placement.read()
    val maximized = ok && placement.showCmd == WinUser.SW_SHOWMAXIMIZED
    placement.clear()
    return maximized
}

/**
 * The native pieces the fullscreen toggle and the title-bar buttons need.
 *
 * Fullscreen is a plain style flip: the saved styles, rect and maximized state
 * are restored on exit. Because the frame stays native, there is no exclusive
 * fullscreen and no Compose resize workaround.
 */
internal object WindowsWindowUtils {
    private var savedFullscreenState: SavedWindowState? = null

    fun setUndecoratedFullscreen(
        window: ComposeWindow,
        fullscreen: Boolean,
    ) {
        val hwnd = HWND(Pointer(window.windowHandle))
        if (fullscreen) enterFullscreen(hwnd) else exitFullscreen(hwnd)
    }

    fun minimize(window: ComposeWindow) {
        user32.CloseWindow(HWND(Pointer(window.windowHandle)))
    }

    fun toggleMaximize(
        window: ComposeWindow,
        maximize: Boolean,
    ) {
        user32.ShowWindow(HWND(Pointer(window.windowHandle)), if (maximize) WinUser.SW_MAXIMIZE else WinUser.SW_RESTORE)
    }

    private fun enterFullscreen(hwnd: HWND) {
        if (savedFullscreenState != null) return
        val maximized = hwnd.isMaximized()
        if (maximized) sendSysCommand(hwnd, SC_RESTORE)
        val style = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
        if (style and WinUser.WS_CAPTION == 0) return
        val exStyle = user32.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE)
        val rect =
            RECT().also {
                user32.GetWindowRect(hwnd, it)
                it.read()
            }
        savedFullscreenState = SavedWindowState(style, exStyle, rect, maximized)
        user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, style and (WinUser.WS_CAPTION or WinUser.WS_THICKFRAME).inv())
        user32.SetWindowLong(
            hwnd,
            WinUser.GWL_EXSTYLE,
            exStyle and (WS_EX_DLGMODALFRAME or WS_EX_WINDOWEDGE or WS_EX_CLIENTEDGE or WS_EX_STATICEDGE).inv(),
        )
        val monitor = user32.MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST)
        val info = WinUser.MONITORINFO()
        user32.GetMonitorInfo(monitor, info)
        info.read()
        val bounds = info.rcMonitor
        user32.SetWindowPos(
            hwnd,
            null,
            bounds.left,
            bounds.top,
            bounds.right - bounds.left,
            bounds.bottom - bounds.top,
            WinUser.SWP_NOZORDER or WinUser.SWP_NOACTIVATE or WinUser.SWP_FRAMECHANGED,
        )
    }

    private fun exitFullscreen(hwnd: HWND) {
        val saved = savedFullscreenState ?: return
        val style = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
        if (style and WinUser.WS_CAPTION != 0) {
            savedFullscreenState = null
            return
        }
        user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, saved.style)
        user32.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, saved.exStyle)
        user32.SetWindowPos(
            hwnd,
            null,
            saved.rect.left,
            saved.rect.top,
            saved.rect.right - saved.rect.left,
            saved.rect.bottom - saved.rect.top,
            WinUser.SWP_NOZORDER or WinUser.SWP_NOACTIVATE or WinUser.SWP_FRAMECHANGED,
        )
        if (saved.maximized) sendSysCommand(hwnd, WinUser.SC_MAXIMIZE)
        savedFullscreenState = null
    }

    private fun sendSysCommand(
        hwnd: HWND,
        command: Int,
    ) {
        user32.SendMessage(hwnd, WinUser.WM_SYSCOMMAND, WPARAM(command.toLong()), LPARAM(0))
    }

    private data class SavedWindowState(
        val style: Int,
        val exStyle: Int,
        val rect: RECT,
        val maximized: Boolean,
    )
}

/** The recursive lookup Animeko uses to reach the Compose Skia layer. */
private fun ComposeWindow.findSkiaLayer(): SkiaLayer? = findComponent(this, SkiaLayer::class.java)

private fun <T : JComponent> findComponent(
    container: Container,
    klass: Class<T>,
): T? {
    val components = container.components.asSequence()
    return components
        .filter { klass.isInstance(it) }
        .ifEmpty { components.filterIsInstance<Container>().mapNotNull { findComponent(it, klass) } }
        .map { klass.cast(it) }
        .firstOrNull()
}

internal interface ExtendedUser32 : User32 {
    fun ScreenToClient(
        hWnd: HWND,
        lpPoint: POINT,
    ): Boolean

    fun GetSystemMetricsForDpi(
        nIndex: Int,
        dpi: Int,
    ): Int

    fun GetDpiForWindow(hWnd: HWND): Int

    companion object {
        val INSTANCE: ExtendedUser32 = Native.load("user32", ExtendedUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}

private val user32: ExtendedUser32 by lazy { ExtendedUser32.INSTANCE }

private const val WM_NCCALCSIZE = 0x0083
private const val WM_NCHITTEST = 0x0084
private const val WM_NCMOUSEMOVE = 0x00A0
private const val WM_NCLBUTTONDOWN = 0x00A1
private const val WM_NCLBUTTONUP = 0x00A2
private const val WM_MOUSEMOVE = 0x0200
private const val WM_LBUTTONDOWN = 0x0201
private const val WM_LBUTTONUP = 0x0202

private const val SC_RESTORE = 0xF120
private const val MONITOR_DEFAULTTONEAREST = 2

private const val WS_EX_DLGMODALFRAME = 0x00000001
private const val WS_EX_WINDOWEDGE = 0x00000100
private const val WS_EX_CLIENTEDGE = 0x00000200
private const val WS_EX_STATICEDGE = 0x00020000
