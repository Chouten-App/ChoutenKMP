package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

expect val httpClient: HttpClient

fun startDevClient(ip: String, onWasmReceived: (ByteArray) -> Unit) {
    val client = httpClient

    GlobalScope.launch {
        try {
            client.webSocket("ws://$ip:9001/dev") {
                println("🔌 Connected to Chouten dev CLI")

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            val json = Json.parseToJsonElement(text).jsonObject
                            println("📩 JSON frame: $json")
                        }

                        is Frame.Binary -> {
                            val bytes = frame.readBytes()
                            onWasmReceived(bytes)
                            //println("📦 Binary frame received: ${bytes.size} bytes")
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ WebSocket error: $e")
        }
    }
}
