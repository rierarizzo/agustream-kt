package com.misgus.agustream.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misgus.agustream.core.repository.PosterSize

/** Card ratio shared by the poster grids, so a card looks the same everywhere. */
private const val POSTER_CARD_ASPECT = 0.58f

/**
 * Nominal poster width for each [PosterSize].
 *
 * It is the minimum cell width of a poster grid and the card width of a poster
 * row, so the catalog grids, Discover, Home and "More like this" stay in sync.
 * The steps are deliberately small.
 */
val PosterSize.cellWidth: Dp
    get() =
        when (this) {
            PosterSize.SMALL -> 210.dp
            PosterSize.DEFAULT -> 230.dp
            PosterSize.LARGE -> 250.dp
        }

/** Height of a poster card (art plus caption) for a horizontal rail. */
val PosterSize.railHeight: Dp
    get() = cellWidth / POSTER_CARD_ASPECT
