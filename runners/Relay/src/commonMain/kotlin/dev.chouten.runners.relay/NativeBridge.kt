package dev.chouten.runners.relay

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import dev.chouten.core.repository.DevClient
import dev.chouten.core.repository.HostEnvironment
import dev.chouten.core.repository.httpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class Log(
    val time: Instant,
    val message: String
)

object RelayLogger {
    var logs: List<Log> = listOf()

    var devClient: DevClient? = null

    fun log(message: String) {
        val now = Clock.System.now()
        logs += Log(now, message)

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

expect object NativeBridge {
    fun initLogger(logger: Any)
    fun initHostEnvironment(host: HostEnvironment)
    fun load(bytes: ByteArray)
    fun add(a: Int, b: Int): Int
    fun callMethod(name: String): String
}