package com.misgus.pengu.ui.components

import androidx.compose.runtime.Composable
import com.misgus.pengu.app.shared.generated.resources.Res
import com.misgus.pengu.app.shared.generated.resources.content_type_movie
import com.misgus.pengu.app.shared.generated.resources.content_type_series
import com.misgus.pengu.app.shared.generated.resources.search_type_movies
import com.misgus.pengu.app.shared.generated.resources.search_type_series
import com.misgus.pengu.core.model.ContentTypes
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
        ContentTypes.MOVIE -> stringResource(Res.string.search_type_movies)
        ContentTypes.SERIES -> stringResource(Res.string.search_type_series)
        else -> null
    }

/**
 * The singular "Movie" / "Series" name for a Stremio content type, or `null` for
 * a type the app does not name.
 *
 * Used inside a billboard's metadata line, where the plural section label would
 * read wrong ("Movies" next to a single title).
 */
@Composable
fun contentTypeName(type: String): String? =
    when {
        type.equals(ContentTypes.MOVIE, ignoreCase = true) -> stringResource(Res.string.content_type_movie)
        ContentTypes.isSeries(type) -> stringResource(Res.string.content_type_series)
        else -> null
    }
