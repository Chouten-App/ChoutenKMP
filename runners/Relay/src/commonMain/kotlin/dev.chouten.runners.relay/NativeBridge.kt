package dev.chouten.runners.relay

import com.inumaki.core.ui.model.DevClient
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


object RelayLogger {
    var logs: List<String> = listOf()

    var devClient: DevClient? = null

    fun log(message: String) {
        logs += message

        println("RelayWASM -> $message")

        devClient?.let { client ->
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

@Serializable
data class HttpResponse(
    val statusCode: Int,
    val body: String?,
    val headers: Map<String, String>
)

expect object NativeBridge {
    fun request(url: String, method: Int): String
    fun initLogger(logger: Any)
    fun initNativeBridge(nativeBridge: NativeBridge)
    fun load(bytes: ByteArray)
    fun add(a: Int, b: Int): Int
    fun callMethod(name: String): String
}