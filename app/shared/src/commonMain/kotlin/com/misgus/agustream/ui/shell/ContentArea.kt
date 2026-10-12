package com.misgus.agustream.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.misgus.agustream.ui.player.PlayerScreen
import com.misgus.agustream.ui.screens.CatalogScreen
import com.misgus.agustream.ui.screens.DetailScreen
import com.misgus.agustream.ui.streams.StreamsRequest
import com.misgus.agustream.ui.theme.AppMotion
import com.misgus.agustream.ui.theme.AppSizes
import com.misgus.agustream.ui.theme.LocalMotionEnabled

/** Host for transient snackbars, so any screen can show one. */
val LocalSnackbarHost = staticCompositionLocalOf<SnackbarHostState?> { null }

/** A [ViewModelStoreOwner] scoped to a pushed route; cleared when the route pops. */
class RouteViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

/**
 * Renders the content area: the current section with the pushed route on top.
 *
 * The section stays composed while a route is open, so its scroll position is
 * never saved and restored — that round-trip lost the within-item offset and
 * scrolled the section back up. It only animates when the tab changes; the route
 * fades in over it and leaves at once when popped.
 *
 * The player renders outside the route transition on purpose: it drives a single
 * shared engine, and a transition would keep the outgoing player alive next to
 * the incoming one.
 */
@Composable
fun ContentArea(
    selected: AppSection,
    navigator: ContentNavigator,
    onSelectSection: (AppSection) -> Unit,
    onOpenStreams: (StreamsRequest) -> Unit,
) {
    val enabled = LocalMotionEnabled.current
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                val fade = AppMotion.spec<Float>(enabled, AppMotion.MEDIUM)
                // `using(null)` disables the size animation, so the crossfade
                // stays on the draw phase.
                (fadeIn(fade) togetherWith fadeOut(fade)).using(null)
            },
            label = "section",
        ) { section ->
            // Sections reserve the floating top bar's height; the padding is
            // constant, so fading the bar never resizes the section's scroll.
            Box(Modifier.fillMaxSize().padding(top = AppSizes.topBarHeight)) {
                SectionHost(section, navigator, onSelectSection, onOpenStreams)
            }
        }

        val route = navigator.current
        if (route is ContentRoute.Player) {
            RouteContent(route) {
                PlayerScreen(
                    target = route.target,
                    onBack = navigator::pop,
                    onPlayNext = { navigator.replace(ContentRoute.Player(it)) },
                )
            }
            return@Box
        }

        AnimatedContent(
            targetState = route,
            transitionSpec = {
                val fade = AppMotion.spec<Float>(enabled, AppMotion.MEDIUM)
                // Only the route animates; the section underneath is left alone.
                (fadeIn(fade) togetherWith ExitTransition.None).using(null)
            },
            label = "route",
        ) { target ->
            if (target != null) {
                RouteContent(target) {
                    when (target) {
                        is ContentRoute.Detail ->
                            DetailScreen(
                                item = target.item,
                                knownSaved = target.knownSaved,
                                onBack = navigator::pop,
                                onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
                                onOpenStreams = onOpenStreams,
                            )
                        is ContentRoute.Catalog ->
                            CatalogScreen(
                                ref = target.ref,
                                onBack = navigator::pop,
                                onOpenDetail = { navigator.push(ContentRoute.Detail(it)) },
                            )
                        // Rendered above, outside the transition.
                        is ContentRoute.Player -> Unit
                    }
                }
            }
        }
    }
}

/** Scopes a route's ViewModels and clears them when the route leaves. */
@Composable
private fun RouteContent(
    key: Any,
    content: @Composable () -> Unit,
) {
    val owner = remember(key) { RouteViewModelStoreOwner() }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        content()
    }
}
