package com.misgus.agustream.ui.theme

import coil3.Bitmap

internal actual fun Bitmap.argbAt(
    x: Int,
    y: Int,
): Int = getPixel(x, y)
