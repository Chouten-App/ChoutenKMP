package com.inumaki.features.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.TopBarAction
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.model.UiConfigProvider


class HomeEntry: FeatureEntry, UiConfigProvider {
    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
        navScope: NavigationScope
    ) {
        builder.composable<HomeRoute> {  }
    }

    override fun getRoute(): AppRoute = HomeRoute

    override fun tryCreateRoute(entry: androidx.navigation.NavBackStackEntry): AppRoute? {
        val routeName = entry.destination.route ?: return null
        if (!routeName.startsWith("Home")) return null

        return try {
            entry.toRoute<HomeRoute>()
        } catch (e: Throwable) {
            println("Failed to decode ExploreRoute: ${e.message}")
            null
        }
    }

    override fun topBarConfig(
        route: AppRoute,
        navController: NavHostController
    ): TopBarConfig? = when (route) {
        is HomeRoute -> TopBarConfig(
            title = "Home",
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