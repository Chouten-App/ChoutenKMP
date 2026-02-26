package dev.chouten.runners.relay

import com.inumaki.core.ui.model.DevClient
import kotlinx.coroutines.launch


object RelayLogger {
    var logs: List<String> = listOf()

    // Optional DevClient; if null, fallback to println
    var devClient: DevClient? = null

    // Called from native C++ / WASM
    fun log(message: String) {
        logs += message

        // Always print locally
        println("RelayWASM -> $message")

        // If a dev client is set, send asynchronously
        devClient?.let { client ->
            // Fire-and-forget coroutine
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    client.sendLog(message)
                } catch (e: Exception) {
                    println("⚠️ Failed to send log to DevClient: $e")
                }
            }
        }
    }
}

data class HttpResponse(
    val statusCode: Int,
    val body: String?,
    val headers: Map<String, String>
)

expect object NativeBridge {
    fun request(url: String, method: Int): HttpResponse
    fun initLogger(logger: Any)
    fun initNativeBridge(nativeBridge: NativeBridge)
    fun load(bytes: ByteArray)
    fun add(a: Int, b: Int): Int
    fun callMethod(name: String): String
}