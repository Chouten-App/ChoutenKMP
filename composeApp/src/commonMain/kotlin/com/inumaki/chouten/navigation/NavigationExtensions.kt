package com.inumaki.chouten.navigation

import androidx.navigation.NavBackStackEntry
import com.inumaki.core.ui.model.AppRoute
import com.inumaki.core.ui.model.FeatureEntry

/**
 * Converts a NavBackStackEntry to its corresponding AppRoute.
 *
 * This extension tries to match the back stack entry with registered
 * feature entries to determine which route it represents.
 *
 * @param featureEntries List of registered feature entries
 * @return The corresponding AppRoute, or null if no match is found
 */
fun NavBackStackEntry.toAppRoute(featureEntries: List<FeatureEntry>): AppRoute? {
    return featureEntries
        .asSequence()
        .mapNotNull { it.tryCreateRoute(this) }
        .firstOrNull()
}