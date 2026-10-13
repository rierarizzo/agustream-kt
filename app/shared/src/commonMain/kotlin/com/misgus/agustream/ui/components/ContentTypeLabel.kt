package com.misgus.agustream.ui.components

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.search_type_movies
import agustream.app.shared.generated.resources.search_type_series
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

/**
 * The "Movies" / "Series" label for a Stremio content type, or `null` for a type
 * the app does not name, so the caller can omit it.
 *
 * One place for the mapping, so a section title and any future caller label the
 * types the same way.
 */
@Composable
fun contentTypeLabel(type: String): String? =
    when (type) {
        "movie" -> stringResource(Res.string.search_type_movies)
        "series" -> stringResource(Res.string.search_type_series)
        else -> null
    }
