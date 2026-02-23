package com.inumaki.chouten.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.inumaki.core.ui.model.AppConfig

/**
 * Main navigation host for the app.
 *
 * Registers all feature modules and their navigation graphs.
 * Each feature is responsible for registering its own screens.
 */
@Composable
fun AppNavHost(
    appConfig: AppConfig,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = appConfig.navController,
        startDestination = appConfig.startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        // Register all feature navigation graphs
        appConfig.featureEntries.forEach { featureEntry ->
            featureEntry.register(
                builder = this,
                navController = appConfig.navController,
                navScope = appConfig.navScope
            )
        }
    }
}