package com.inumaki.chouten

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.inumaki.chouten.ui.AppRoot

/**
 * Main application entry point.
 * Keep this file minimal - all logic should be in separate files.
 */
@Composable
fun App(
    headingSource: HeadingSource,
    dataStore: DataStore<Preferences>
) {
    AppRoot(
        headingSource = headingSource,
        dataStore = dataStore
    )
}