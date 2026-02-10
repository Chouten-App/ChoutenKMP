package dev.chouten.features.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.inumaki.core.ui.model.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel(private val dataStore: DataStore<Preferences>, private val onCliChange: (String) -> Unit) : ViewModel() {
    private val USE_BLUR = booleanPreferencesKey("use_blur")
    private val USE_LIQUID_GLASS = booleanPreferencesKey("use_liquid_glass")
    val CHOUTEN_CLI = stringPreferencesKey("chouten_cli")
    val useBlur: StateFlow<Boolean?> =
        dataStore.data
            .map { prefs -> prefs[USE_BLUR] ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    val useLiquidGlass: StateFlow<Boolean?> =
        dataStore.data
            .map { prefs -> prefs[USE_LIQUID_GLASS] ?: false }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    val cliIP: StateFlow<String> =
        dataStore.data
            .map { prefs -> prefs[CHOUTEN_CLI] ?: "" }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = ""
            )

    init {}

    fun setUseBlur(enabled: Boolean) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[USE_BLUR] = enabled
            }
        }
    }

    fun setUseLiquidGlass(enabled: Boolean) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[USE_LIQUID_GLASS] = enabled
            }
        }
    }

    fun setChoutenCLI(value: String) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[CHOUTEN_CLI] = value
            }
            onCliChange(value)
        }
    }
}