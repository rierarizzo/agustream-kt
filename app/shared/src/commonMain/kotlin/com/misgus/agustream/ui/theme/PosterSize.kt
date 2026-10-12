package com.misgus.agustream.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.repository.PosterSize

/** Card ratio shared by the poster grids, so a card looks the same everywhere. */
private const val POSTER_CARD_ASPECT = 0.58f

/** How much a poster shrinks on a compact window (phone). */
private const val COMPACT_SCALE = 0.45f

/** The width the user chose, before the window is taken into account. */
private val PosterSize.nominalCellWidth: Dp
    get() =
        when (this) {
            PosterSize.SMALL -> 210.dp
            PosterSize.DEFAULT -> 230.dp
            PosterSize.LARGE -> 250.dp
        }

/**
 * Poster width for [windowSize].
 *
 * It is the minimum cell width of a poster grid and the card width of a poster
 * row, so the catalog grids, Discover, Home and "More like this" stay in sync.
 * Compact windows scale it down, so a phone shows more than one card while the
 * user's small / default / large choice still makes a difference.
 */
fun PosterSize.cellWidth(windowSize: WindowSize): Dp =
    if (windowSize == WindowSize.COMPACT) nominalCellWidth * COMPACT_SCALE else nominalCellWidth

/** Height of a poster card (art plus caption) for [windowSize]. */
fun PosterSize.railHeight(windowSize: WindowSize): Dp = cellWidth(windowSize) / POSTER_CARD_ASPECT
