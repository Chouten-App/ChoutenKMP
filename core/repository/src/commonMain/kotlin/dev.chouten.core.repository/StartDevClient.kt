package dev.chouten.core.repository

import com.inumaki.core.ui.model.DevClient
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

expect val httpClient: HttpClient

private fun buildWsUrl(input: String): String {
    var url = input.trim()

    // Remove trailing slashes
    url = url.trimEnd('/')

    // Check if it already has a protocol
    val hasProtocol = url.startsWith("ws://") || url.startsWith("wss://")

    // Check if it has a port (look for :digits after the host)
    val hostPart = if (hasProtocol) url.substringAfter("://") else url
    val hasPort = hostPart.contains(Regex(":\\d+"))

    // Build the URL
    return when {
        hasProtocol && hasPort -> "$url/dev"
        hasProtocol && !hasPort -> "$url:9001/dev"
        !hasProtocol && hasPort -> "ws://$url/dev"
        else -> "ws://$url:9001/dev"
    }
}

fun startDevClient(
    ip: String,
    onWasmReceived: (ByteArray, DevClient) -> Unit
): DevClient {
    val client = httpClient
    var session: WebSocketSession? = null

    // Build WebSocket URL - handle custom ports and protocols
    val wsUrl = buildWsUrl(ip)

    // Create the DevClient first
    val devClient = object : DevClient {
        override suspend fun sendLog(message: String) {
            session?.send(Frame.Text(message))
        }
    }

    GlobalScope.launch {
        try {
            println("🔗 Connecting to: $wsUrl")
            client.webSocket(wsUrl) {
                println("🔌 Connected to Chouten dev CLI")
                session = this

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val json = Json.parseToJsonElement(text).jsonObject
                            println("📩 JSON frame: $json")
                        }

                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            onWasmReceived(bytes, devClient) // pass the DevClient here
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ WebSocket error: $e")
        }
    }

    return devClient
}
