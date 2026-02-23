package com.inumaki.features.discover

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.DiscoverRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.TopBarAction
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.model.UiConfigProvider


class DiscoverEntry: FeatureEntry, UiConfigProvider {
    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
        navScope: NavigationScope,
    ) {
        builder.composable<DiscoverRoute> {  }
    }
    override fun getRoute(): AppRoute = DiscoverRoute

    override fun tryCreateRoute(entry: androidx.navigation.NavBackStackEntry): AppRoute? {
        val routeName = entry.destination.route ?: return null
        if (!routeName.startsWith("Discover")) return null

        return try {
            entry.toRoute<DiscoverRoute>()
        } catch (e: Throwable) {
            println("Failed to decode ExploreRoute: ${e.message}")
            null
        }
    }

    override fun topBarConfig(
        route: AppRoute,
        navController: NavHostController
    ): TopBarConfig? = when (route) {
        is DiscoverRoute -> TopBarConfig(
            title = "Discover",
            actions = listOf(
                TopBarAction(
                    "arrow-down-wide-short-solid-full.svg",
                    contentDescription = "filter",
                    onClick = {
                        println("Filter clicked")
                    }
                )
            )
        )
        else -> null
    }
}