package com.inumaki.chouten.dev

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.inumaki.core.ui.model.DevClient
import dev.chouten.core.repository.startDevClient
import dev.chouten.features.settings.SettingsViewModel
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages the dev client connection for WASM module development.
 *
 * Handles:
 * - Initializing connections to the Chouten CLI
 * - Loading WASM binaries
 * - Managing the native bridge
 * - Logging and debugging
 */
class DevClientManager {
    private var devClient: DevClient? = null

    /**
     * Initialize a connection to the dev client.
     *
     * @param cliIP The IP address of the Chouten CLI
     */
    fun initialize(cliIP: String) {
        devClient = startDevClient(cliIP) { wasm, client ->
            println("📦 Binary frame received: ${wasm.size} bytes")
            NativeBridge.load(wasm)
            RelayLogger.devClient = client

            try {
                val result = NativeBridge.callMethod("discover_wrapper")
                println("✅ WASM method call result: $result")
            } catch (e: Exception) {
                println("❌ NativeBridge error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Disconnect from the dev client.
     */
    fun disconnect() {
        devClient = null
        RelayLogger.devClient = null
    }

    /**
     * Initialize from saved DataStore preferences.
     * Automatically connects if a CLI IP is saved.
     */
    suspend fun initializeFromDataStore(
        dataStore: DataStore<Preferences>
    ) {
        val cliIP = dataStore.data
            .map { prefs ->
                prefs[stringPreferencesKey("chouten_cli")] ?: ""
            }
            .first()

        if (cliIP.isNotEmpty()) {
            println("🔌 Auto-connecting to saved CLI: $cliIP")
            initialize(cliIP)
        }
    }

    /**
     * Check if currently connected to a dev client.
     */
    val isConnected: Boolean
        get() = devClient != null
}