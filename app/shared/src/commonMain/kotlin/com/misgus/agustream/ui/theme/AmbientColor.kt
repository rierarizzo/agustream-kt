package com.misgus.agustream.ui.theme

import androidx.compose.ui.graphics.Color
import coil3.Bitmap
import coil3.Image
import coil3.toBitmap

/**
 * Reads the packed ARGB value of the pixel at ([x], [y]).
 *
 * Coil exposes a different bitmap type per platform (Skia on JVM, the framework
 * bitmap on Android), so pixel access needs a tiny platform shim. The algorithm
 * itself stays in common code.
 */
internal expect fun Bitmap.argbAt(
    x: Int,
    y: Int,
): Int

/**
 * Samples the dominant colour from [image] to tint the app background.
 *
 * Pixels are bucketed by hue and weighted by how vivid they are, then the
 * heaviest bucket wins. Averaging all pixels together (as a plain mean does)
 * blends complementary hues — a green image with a red accent can come out red —
 * whereas the histogram keeps hues apart and reports the one the artwork is
 * actually made of.
 *
 * Returns `null` when the artwork has no meaningful hue, so the caller can fall
 * back to a neutral colour.
 */
internal fun ambientColor(image: Image): Color? {
    val width = image.width
    val height = image.height
    if (width <= 0 || height <= 0) return null

    val bitmap = image.toBitmap()
    val stepX = (width / SAMPLES).coerceAtLeast(1)
    val stepY = (height / SAMPLES).coerceAtLeast(1)

    val hueWeight = FloatArray(HUE_BINS)
    val hueR = FloatArray(HUE_BINS)
    val hueG = FloatArray(HUE_BINS)
    val hueB = FloatArray(HUE_BINS)

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val argb = bitmap.argbAt(x, y)
            val alpha = ((argb ushr 24) and 0xFF) / 255.0
            if (alpha > MIN_ALPHA) {
                val r = ((argb ushr 16) and 0xFF) / 255.0
                val g = ((argb ushr 8) and 0xFF) / 255.0
                val b = (argb and 0xFF) / 255.0
                val max = maxOf(r, g, b)
                val min = minOf(r, g, b)
                val delta = max - min
                val saturation = if (max <= 0.0) 0.0 else delta / max
                if (max >= MIN_VALUE && saturation >= MIN_SATURATION) {
                    val bin = (hueDegrees(r, g, b, max, delta) / 360.0 * HUE_BINS).toInt().coerceIn(0, HUE_BINS - 1)
                    // Area first, with a modest bonus for vivid pixels: a large
                    // muted area should outvote a small saturated accent.
                    val weight = (0.5 + saturation).toFloat()
                    hueWeight[bin] += weight
                    hueR[bin] += (r * weight).toFloat()
                    hueG[bin] += (g * weight).toFloat()
                    hueB[bin] += (b * weight).toFloat()
                }
            }
            x += stepX
        }
        y += stepY
    }

    // Score each bucket with its neighbours, so a hue that straddles a boundary
    // is not split between two bins.
    var bestBin = -1
    var bestScore = 0f
    for (i in 0 until HUE_BINS) {
        val score =
            hueWeight[i] +
                0.5f * hueWeight[(i - 1 + HUE_BINS) % HUE_BINS] +
                0.5f * hueWeight[(i + 1) % HUE_BINS]
        if (score > bestScore) {
            bestScore = score
            bestBin = i
        }
    }
    if (bestBin < 0 || bestScore <= 0f) return null

    // Average the colours around the winning hue and lift them to a consistent,
    // glow-friendly brightness.
    var r = 0f
    var g = 0f
    var b = 0f
    var weight = 0f
    for (offset in -1..1) {
        val i = (bestBin + offset + HUE_BINS) % HUE_BINS
        r += hueR[i]
        g += hueG[i]
        b += hueB[i]
        weight += hueWeight[i]
    }
    if (weight <= 0f) return null

    r /= weight
    g /= weight
    b /= weight
    val max = maxOf(r, g, b)
    if (max <= 0f) return null
    val scale = GLOW_LEVEL / max
    return Color(
        red = (r * scale).coerceIn(0f, 1f),
        green = (g * scale).coerceIn(0f, 1f),
        blue = (b * scale).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

/** Hue of an RGB colour, in degrees within [0, 360). */
private fun hueDegrees(
    r: Double,
    g: Double,
    b: Double,
    max: Double,
    delta: Double,
): Double {
    if (delta == 0.0) return 0.0
    val raw =
        when (max) {
            r -> ((g - b) / delta) % 6.0
            g -> ((b - r) / delta) + 2.0
            else -> ((r - g) / delta) + 4.0
        }
    return (raw * 60.0 + 360.0) % 360.0
}

private const val SAMPLES = 24
private const val HUE_BINS = 24
private const val MIN_ALPHA = 0.2
private const val MIN_VALUE = 0.08
private const val MIN_SATURATION = 0.10
private const val GLOW_LEVEL = 0.7f
