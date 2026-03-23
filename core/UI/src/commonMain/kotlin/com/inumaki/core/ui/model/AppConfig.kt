package com.inumaki.core.ui.model

import androidx.compose.runtime.Immutable
import androidx.navigation.NavHostController

@Immutable
class AppConfig(
    val navController: NavHostController,
    val navScope: NavigationScope,
    val startDestination: AppRoute,
    val featureEntries: List<FeatureEntry>,
    val uiConfigProvider: List<UiConfigProvider>
)