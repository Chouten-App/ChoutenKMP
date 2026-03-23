package com.inumaki.core.ui.model

import androidx.navigation.NavHostController

/**
 * A unified configuration provider for feature-level UI customization.
 * Each feature can override topBar and bottomBar appearance based on route.
 */
interface UiConfigProvider {
    /**
     * Supplies a top app bar configuration for the requested [route], or `null` to hide it.
     */
    fun topBarConfig(route: AppRoute, navController: NavHostController): TopBarConfig?
}

data class TopBarConfig(
    val title: String = "",
    val showNavigationIcon: Boolean = false,
    val onNavigationClick: (() -> Unit)? = null,
    val actions: List<TopBarAction> = emptyList()
)

/**
 * Describes a single action icon rendered inside the top app bar.
 *
 * @property icon Icon vector displayed for the action.
 * @property contentDescription Accessibility text for screen readers.
 * @property onClick Executed when the action is tapped.
 */
data class TopBarAction(
    val icon: String,
    val contentDescription: String?,
    val onClick: () -> Unit
)