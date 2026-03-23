package com.inumaki.chouten.common

import com.inumaki.core.ui.model.FeatureEntry
import com.inumaki.core.ui.model.UiConfigProvider
import com.inumaki.features.discover.DiscoverEntry
import com.inumaki.features.home.HomeEntry
import com.inumaki.features.repo.RepoEntry
import dev.chouten.features.settings.SettingsEntry

fun getFeatures(): Pair<List<FeatureEntry>, List<UiConfigProvider>> {
    val features: List<FeatureEntry> = listOf(
        HomeEntry(),
        DiscoverEntry(),
        RepoEntry(),
        SettingsEntry()
    )

    val uiConfigProvider: List<UiConfigProvider> = features.filterIsInstance<UiConfigProvider>()

    return Pair(features, uiConfigProvider)
}