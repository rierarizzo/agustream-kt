package com.misgus.pengu.ui.components

import coil3.Bitmap
import coil3.size.Size
import coil3.transform.Transformation

/**
 * Trims the transparent border off an image.
 *
 * Stremio logos often ship as a wordmark centred on a fixed transparent canvas:
 * a series logo can be a 50px strip inside an 800x310 PNG, and the margin varies
 * per title. Drawn through `ContentScale.Fit`, that whole canvas is scaled, so
 * the visible logo reads small and the box's optical height and max-width guard
 * measure padding instead of artwork. Trimming first makes every logo fill its
 * box the same way, so the billboards size logos consistently.
 *
 * Pixels at or below [alphaThreshold] count as transparent; the small threshold
 * drops the faint anti-aliasing a crop can leave at the edge without keeping a
 * stray pixel that would defeat the trim. An image that is already opaque, or
 * one whose content reaches every edge, is returned untouched.
 */
class TrimTransparentTransformation(
    private val alphaThreshold: Int = DEFAULT_ALPHA_THRESHOLD,
) : Transformation() {
    override val cacheKey: String = "trimTransparent(threshold=$alphaThreshold)"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap = trimTransparent(input, alphaThreshold)

    companion object {
        /** Highest alpha that still counts as transparent. */
        const val DEFAULT_ALPHA_THRESHOLD = 8
    }
}

/**
 * Crops [bitmap] to the bounding box of its content, or returns it untouched
 * when there is nothing to trim.
 *
 * Platform-specific because Coil's [Bitmap] is `android.graphics.Bitmap` on
 * Android and a Skia bitmap on the JVM.
 */
internal expect fun trimTransparent(
    bitmap: Bitmap,
    alphaThreshold: Int,
): Bitmap

/**
 * Bounding box of the pixels a platform reports as content.
 *
 * [isContent] receives the pixel index in row-major order and returns whether
 * that pixel is above the alpha threshold. The scan is shared so only the pixel
 * read differs between platforms.
 */
internal inline fun contentBounds(
    width: Int,
    height: Int,
    isContent: (index: Int) -> Boolean,
): ContentBounds {
    var left = width
    var top = height
    var right = -1
    var bottom = -1
    var index = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (isContent(index)) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
            index++
        }
    }
    return ContentBounds(left, top, right, bottom)
}

/** Pixel rectangle of an image's content; empty when no pixel is above the threshold. */
internal data class ContentBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val isEmpty: Boolean get() = right < left || bottom < top
    val width: Int get() = right - left + 1
    val height: Int get() = bottom - top + 1
}
