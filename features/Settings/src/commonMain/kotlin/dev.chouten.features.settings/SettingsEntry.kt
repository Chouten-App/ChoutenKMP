package dev.chouten.features.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.SettingsRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.TopBarAction
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.model.UiConfigProvider


class SettingsEntry: FeatureEntry, UiConfigProvider {
    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
        navScope: NavigationScope
    ) {
        builder.composable<SettingsRoute> {  }
    }
    override fun getRoute(): AppRoute = SettingsRoute

    override fun tryCreateRoute(entry: androidx.navigation.NavBackStackEntry): AppRoute? {
        val routeName = entry.destination.route ?: return null
        if (!routeName.startsWith("Settings")) return null

        return try {
            entry.toRoute<SettingsRoute>()
        } catch (e: Throwable) {
            println("Failed to decode ExploreRoute: ${e.message}")
            null
        }
    }

    override fun topBarConfig(
        route: AppRoute,
        navController: NavHostController
    ): TopBarConfig? = null
}