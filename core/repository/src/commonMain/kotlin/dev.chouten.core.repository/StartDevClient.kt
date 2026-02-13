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

fun startDevClient(
    ip: String,
    onWasmReceived: (ByteArray, DevClient) -> Unit
): DevClient {
    val client = httpClient
    var session: WebSocketSession? = null

    // Create the DevClient first
    val devClient = object : DevClient {
        override suspend fun sendLog(message: String) {
            session?.send(Frame.Text(message))
        }
    }

    GlobalScope.launch {
        try {
            client.webSocket("ws://$ip:9001/dev") {
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
