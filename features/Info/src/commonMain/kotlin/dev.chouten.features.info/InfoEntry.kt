package dev.chouten.features.info

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.RepoRoute
import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.HomeRoute
import com.inumaki.core.ui.model.InfoRoute
import com.inumaki.core.ui.model.NavigationScope
import com.inumaki.core.ui.model.TopBarAction
import com.inumaki.core.ui.model.TopBarConfig
import com.inumaki.core.ui.model.UiConfigProvider


class InfoEntry: FeatureEntry, UiConfigProvider {
    override fun register(
        builder: NavGraphBuilder,
        navController: NavHostController,
        navScope: NavigationScope
    ) {
        builder.composable<InfoRoute> { InfoView() }
    }
    override fun getRoute(): AppRoute = InfoRoute

    override fun tryCreateRoute(entry: androidx.navigation.NavBackStackEntry): AppRoute? {
        val routeName = entry.destination.route ?: return null
        if (!routeName.startsWith("Info")) return null

        return try {
            entry.toRoute<InfoRoute>()
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