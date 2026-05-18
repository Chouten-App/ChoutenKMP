package dev.chouten.core.repository

import dev.chouten.core.repository.DevClient
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Instant

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

sealed interface RtValue {
    data class RtObject(val fields: Map<String, RtValue>) : RtValue
    data class RtArray(val items: List<RtValue>) : RtValue
    data class RtString(val value: String) : RtValue
    data class RtNumber(val value: Double) : RtValue
    data class RtBool(val value: Boolean) : RtValue
    data object RtNull : RtValue
}

sealed class SourceOperation(val functionName: String) {
    data object Search : SourceOperation("search")
    data object Details : SourceOperation("details")
    data object Chapters : SourceOperation("chapters")
    data object Pages : SourceOperation("pages")
    data object Stream : SourceOperation("stream")
}
