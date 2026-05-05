package com.inumaki.chouten.dev

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.chouten.core.repository.DevClient
import dev.chouten.core.repository.startDevClient
import dev.chouten.runners.relay.NativeBridge
import dev.chouten.runners.relay.RelayLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var wasmBytes: ByteArray = ByteArray(0)

    private val _discoverResult = MutableStateFlow<String?>(null)
    val discoverResult: StateFlow<String?> = _discoverResult.asStateFlow()

    /**
     * Initialize a connection to the dev client.
     *
     * @param cliIP The IP address of the Chouten CLI
     */
    fun initialize(cliIP: String) {
        devClient = startDevClient(cliIP) { wasm, client ->
            wasmBytes = wasm
            RelayLogger.devClient = client
            callDiscover()
        }
    }

    fun callDiscover() {
        NativeBridge.load(wasmBytes)

        try {
            val result = NativeBridge.callMethod("discover_wrapper")
            println("✅ WASM method call result: $result")
            _discoverResult.value = result
        } catch (e: Exception) {
            println("❌ NativeBridge error: ${e.message}")
            e.printStackTrace()
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