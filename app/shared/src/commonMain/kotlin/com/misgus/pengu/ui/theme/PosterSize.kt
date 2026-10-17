package com.misgus.pengu.ui.theme

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.pengu.core.repository.PosterSize

/** Card ratio shared by the poster grids, so a card looks the same everywhere. */
const val POSTER_CARD_ASPECT = 0.58f

/**
 * Ratio of a poster's artwork on its own.
 *
 * A card with its caption hidden ([posterCardAspect]) collapses to just the
 * artwork, so the freed caption space is dropped instead of stretching the art
 * and cropping its sides.
 */
const val POSTER_ART_ASPECT = 2f / 3f

/** How much a poster shrinks on a compact window (phone). */
private const val COMPACT_SCALE = 0.65f

/** The width the user chose, before the window is taken into account. */
private val PosterSize.nominalCellWidth: Dp
    get() =
        when (this) {
            PosterSize.DEFAULT -> 210.dp
            PosterSize.LARGE -> 230.dp
        }

/**
 * Card width of a horizontal poster row for [windowSize].
 *
 * The size setting only drives these rows (the Home catalogs, the search
 * results and "More like this"); grids keep a fixed layout through [gridCells].
 * Compact windows scale it down, so a phone shows bigger cards than the smallest
 * desktop choice while the user's default / large pick still makes a difference.
 */
fun PosterSize.cellWidth(windowSize: WindowSize): Dp =
    if (windowSize == WindowSize.COMPACT) nominalCellWidth * COMPACT_SCALE else nominalCellWidth

/** Posters per row a grid keeps on a phone, whatever the size setting says. */
private const val COMPACT_GRID_COLUMNS = 3

/** Base cell width a grid uses on a wider window, so the grid stays responsive. */
private val GRID_CELL_WIDTH = 210.dp

/**
 * Columns a poster grid lays out for [windowSize].
 *
 * Grids ignore the poster size setting: a phone keeps [COMPACT_GRID_COLUMNS]
 * per row and a wider window fits as many [GRID_CELL_WIDTH] cells as it can.
 * [availableWidth] is the grid's width; [horizontalPadding] and [spacing] are
 * its content padding and the gap between cells.
 */
fun gridColumnCount(
    windowSize: WindowSize,
    availableWidth: Dp,
    horizontalPadding: Dp,
    spacing: Dp,
): Int =
    if (windowSize == WindowSize.COMPACT) {
        COMPACT_GRID_COLUMNS
    } else {
        val usable = availableWidth - horizontalPadding * 2
        maxOf(((usable + spacing) / (GRID_CELL_WIDTH + spacing)).toInt(), 1)
    }

/** `GridCells` for a loaded poster grid, matching [gridColumnCount]. */
fun gridCells(windowSize: WindowSize): GridCells =
    if (windowSize == WindowSize.COMPACT) GridCells.Fixed(COMPACT_GRID_COLUMNS) else GridCells.Adaptive(GRID_CELL_WIDTH)

/** Card ratio for a poster: the art plus its caption, or just the art when [hideLabels]. */
fun posterCardAspect(hideLabels: Boolean): Float = if (hideLabels) POSTER_ART_ASPECT else POSTER_CARD_ASPECT

/** Height of a poster card [cardWidth] wide: the art plus its caption, or just the art when [hideLabels]. */
fun posterCardHeight(
    cardWidth: Dp,
    hideLabels: Boolean,
): Dp = cardWidth / posterCardAspect(hideLabels)
