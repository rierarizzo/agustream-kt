package com.misgus.agustream.ui.shell

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.misgus.agustream.core.player.PlaybackTarget
import com.misgus.agustream.ui.player.PlayerScreen
import com.misgus.agustream.ui.screens.CatalogScreen
import com.misgus.agustream.ui.screens.DetailScreen
import com.misgus.agustream.ui.streams.StreamsDialog
import com.misgus.agustream.ui.streams.StreamsRequest
import kotlinx.coroutines.launch

/** Host for transient snackbars, so any screen can show one. */
val LocalSnackbarHost = staticCompositionLocalOf<SnackbarHostState?> { null }

/** A [ViewModelStoreOwner] scoped to a pushed route; cleared when the route pops. */
class RouteViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

/**
 * Renders the content area: the section host, or the current pushed route
 * wrapped in its own [ViewModelStoreOwner] so its ViewModels are cleared on pop.
 */
@Composable
fun ContentArea(
    selected: AppSection,
    navigator: ContentNavigator,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val current = navigator.current
    if (current == null) {
        SectionHost(selected, navigator, onOpenStreams)
        return
    }

    val owner = remember(current) { RouteViewModelStoreOwner() }
    androidx.compose.runtime.DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        when (current) {
            is ContentRoute.Detail -> DetailScreen(
                item = current.item,
                onBack = navigator::pop,
                onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
                onOpenStreams = onOpenStreams,
            )
            is ContentRoute.Catalog -> CatalogScreen(
                ref = current.ref,
                onBack = navigator::pop,
                onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
            )
            is ContentRoute.Player -> PlayerScreen(
                target = current.target,
                onBack = navigator::pop,
            )
        }
    }
}
