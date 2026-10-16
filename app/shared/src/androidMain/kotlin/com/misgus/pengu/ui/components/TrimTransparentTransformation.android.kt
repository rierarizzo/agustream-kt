package com.misgus.pengu.ui.components

import android.graphics.Bitmap

/** Android trim: read the packed ARGB pixels, then crop the content rectangle. */
internal actual fun trimTransparent(
    bitmap: Bitmap,
    alphaThreshold: Int,
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0 || !bitmap.hasAlpha()) return bitmap
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val bounds = contentBounds(width, height) { index -> ((pixels[index] ushr 24) and 0xFF) > alphaThreshold }
    if (bounds.isEmpty || (bounds.width == width && bounds.height == height)) return bitmap
    return Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width, bounds.height)
}
