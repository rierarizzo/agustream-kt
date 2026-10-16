package com.misgus.pengu.ui.components

import coil3.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Rect

/** Skia trim: read the alpha plane, then crop by drawing the content rectangle. */
internal actual fun trimTransparent(
    bitmap: Bitmap,
    alphaThreshold: Int,
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0 || bitmap.isOpaque) return bitmap
    val info = ImageInfo(ColorInfo(ColorType.RGBA_8888, ColorAlphaType.PREMUL, null), width, height)
    val pixels = bitmap.readPixels(info, width * 4, 0, 0) ?: return bitmap
    val bounds = contentBounds(width, height) { index -> (pixels[index * 4 + 3].toInt() and 0xFF) > alphaThreshold }
    if (bounds.isEmpty || (bounds.width == width && bounds.height == height)) return bitmap
    val out = Bitmap()
    val outInfo = ImageInfo(ColorInfo(ColorType.RGBA_8888, ColorAlphaType.PREMUL, null), bounds.width, bounds.height)
    if (!out.allocPixels(outInfo)) return bitmap
    // The Canvas and the Image wrap native resources, so both are closed.
    Image.makeFromBitmap(bitmap).use { image ->
        Canvas(out).use { canvas ->
            canvas.drawImageRect(
                image,
                Rect.makeXYWH(bounds.left.toFloat(), bounds.top.toFloat(), bounds.width.toFloat(), bounds.height.toFloat()),
                Rect.makeWH(bounds.width.toFloat(), bounds.height.toFloat()),
            )
        }
    }
    return out
}
