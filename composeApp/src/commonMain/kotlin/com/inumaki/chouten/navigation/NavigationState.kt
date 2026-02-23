package com.inumaki.chouten.navigation

import androidx.navigation.NavBackStackEntry
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.PresentationStyle
import com.inumaki.core.ui.model.presentationStyle

/**
 * Represents the current navigation state of the app.
 *
 * @property fullscreenRoute The current fullscreen route (Discover, Home, Repo)
 * @property topRoute The topmost route in the navigation stack
 */
data class NavigationState(
    val fullscreenRoute: AppRoute?,
    val topRoute: AppRoute?
) {
    companion object {
        /**
         * Creates a NavigationState from the current navigation back stack.
         *
         * Logic:
         * - Finds the last fullscreen route for the fullscreen overlay
         * - Gets the topmost route for sheet overlays
         */
        fun from(
            backStackEntries: List<NavBackStackEntry>,
            featureEntries: List<FeatureEntry>
        ): NavigationState {
            val routes = backStackEntries.mapNotNull { entry ->
                entry.toAppRoute(featureEntries)
            }

            val fullscreenRoute = routes.lastOrNull { route ->
                route.presentationStyle() == PresentationStyle.Fullscreen
            }

            val topRoute = routes.lastOrNull()

            return NavigationState(
                fullscreenRoute = fullscreenRoute,
                topRoute = topRoute
            )
        }
    }
}