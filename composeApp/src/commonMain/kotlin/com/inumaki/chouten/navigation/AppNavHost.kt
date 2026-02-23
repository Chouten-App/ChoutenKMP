package com.inumaki.chouten.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideInHorizontally(
                animationSpec = tween(300, easing = EaseIn),
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutHorizontally(
                animationSpec = tween(300, easing = EaseOut),
            )
        },
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