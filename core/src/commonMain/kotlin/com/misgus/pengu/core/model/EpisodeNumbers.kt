package com.misgus.pengu.core.model

/**
 * A season number.
 *
 * A value class so a season cannot be passed where an episode is expected, or
 * where any other `Int` is expected, without the compiler noticing. Reach the
 * number with [number].
 */
@JvmInline
value class Season(
    val number: Int,
) {
    override fun toString(): String = number.toString()
}

/**
 * An episode number inside a season.
 *
 * A value class so an episode cannot be passed where a season is expected, or
 * where any other `Int` is expected, without the compiler noticing. Reach the
 * number with [number].
 */
@JvmInline
value class Episode(
    val number: Int,
) {
    override fun toString(): String = number.toString()
}
