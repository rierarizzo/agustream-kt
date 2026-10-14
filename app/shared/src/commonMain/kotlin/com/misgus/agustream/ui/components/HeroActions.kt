package com.misgus.agustream.ui.components

import agustream.app.shared.generated.resources.Res
import agustream.app.shared.generated.resources.action_more_info
import agustream.app.shared.generated.resources.detail_my_list
import agustream.app.shared.generated.resources.detail_rate
import agustream.app.shared.generated.resources.status_watched
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misgus.agustream.ui.icons.AppIcons
import com.misgus.agustream.ui.theme.AppSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * State of the icon actions next to a billboard's call to action.
 *
 * The home billboard only uses the save flags; the detail screen also uses the
 * rating and watched ones. The defaults keep a screen from wiring the fields it
 * does not show.
 */
data class HeroActionState(
    val saved: Boolean = false,
    val savedBusy: Boolean = false,
    val savedLoading: Boolean = false,
    val watched: Boolean = false,
    val watchedBusy: Boolean = false,
    val watchedLoading: Boolean = false,
    val rating: Int? = null,
    val ratingLoading: Boolean = false,
)

/**
 * The icon-only actions next to a billboard's call to action.
 *
 * Save is always present; rate and watched are added when their handlers are
 * passed (the detail screen), and [onInfo] appends the "more info" action (the
 * home billboard, where it replaces the old labelled button). One definition
 * keeps these buttons identical wherever a billboard appears.
 */
@Composable
fun HeroActionRow(
    actions: HeroActionState,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
    onRate: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        AppIconButton(
            icon = if (actions.saved) AppIcons.Filled.Favorite else AppIcons.Outlined.FavoriteBorder,
            contentDescription = stringResource(Res.string.detail_my_list),
            onClick = onToggleSaved,
            enabled = !actions.savedBusy,
            selected = actions.saved,
            loading = actions.savedLoading,
        )
        onRate?.let { onRate ->
            AppIconButton(
                icon = if (actions.rating != null) AppIcons.Filled.Star else AppIcons.Outlined.StarBorder,
                contentDescription = stringResource(Res.string.detail_rate),
                onClick = onRate,
                selected = actions.rating != null,
                loading = actions.ratingLoading,
            )
        }
        onToggleWatched?.let { onToggleWatched ->
            AppIconButton(
                icon = if (actions.watched) AppIcons.Filled.CheckCircle else AppIcons.Outlined.CheckCircle,
                contentDescription = stringResource(Res.string.status_watched),
                onClick = onToggleWatched,
                enabled = !actions.watchedBusy,
                selected = actions.watched,
                loading = actions.watchedLoading,
            )
        }
        onInfo?.let { onInfo ->
            AppIconButton(
                icon = AppIcons.Outlined.Info,
                contentDescription = stringResource(Res.string.action_more_info),
                onClick = onInfo,
            )
        }
    }
}
