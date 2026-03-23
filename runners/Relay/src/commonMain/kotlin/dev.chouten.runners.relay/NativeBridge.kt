package dev.chouten.runners.relay

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.inumaki.core.ui.model.DevClient
import dev.chouten.core.repository.httpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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

@OptIn(ExperimentalAtomicApi::class)
object NativeContext {
    private val nextId = AtomicInt(1)
    private val documents = mutableMapOf<Int, Document>()
    private val elements = mutableMapOf<Int, Element>()

    inline fun <reified T> encode(value: T): String =
        Json.encodeToString(value)

    fun request(url: String, method: Int): String {
        println("Requesting url: $url, method: $method")

        return runBlocking {
            val response = withContext(Dispatchers.IO) {
                httpClient.request(url) {
                    // Configure your request here
                    this.method = when (method) {
                        0 -> HttpMethod.Get
                        1 -> HttpMethod.Post
                        // Add other methods as needed
                        else -> HttpMethod.Get
                    }
                }
            }

            val httpResponse = HttpResponse(
                statusCode = response.status.value,
                body = response.bodyAsText(),
                headers = emptyMap()//response.headers.toMap(),
            )
            println("Response -> $httpResponse")

            val jsonString = Json.encodeToString(httpResponse)
            println("JSON Length: ${jsonString.length}")  // Debug
            jsonString
        }
    }

    fun html_parse(html: String): Int {
        val sanitizedHtml = html
            .replace("\\&quot;", "\"")
            .replace("&quot;", "\"")
            .replace("\\\"", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

        val doc = Ksoup.parse(sanitizedHtml)
        val id = nextId.fetchAndAdd(1)
        documents[id] = doc

        return id
    }

    fun html_query_selector(docId: Int, query: String): Int {
        val doc = documents[docId] ?: return -1

        val element = doc.selectFirst(query) ?: return -1

        val id = nextId.fetchAndAdd(1)
        elements[id] = element

        return id
    }
    fun html_query_selector_all(docId: Int, query: String): List<Int> {
        val doc = documents[docId] ?: return emptyList()
        val matchedElements = doc.select(query)

        return matchedElements.map { element ->
            val elementId = nextId.fetchAndAdd(1)
            elements[elementId] = element
            elementId
        }
    }

    fun node_query_selector(nodeId: Int, query: String): Int {
        val parent = elements[nodeId] ?: return -1

        val element = parent.selectFirst(query) ?: return -1

        val id = nextId.fetchAndAdd(1)
        elements[id] = element

        return id
    }
    fun node_text(nodeId: Int): String {
        val element = elements[nodeId]
        return element?.text() ?: ""
    }
    fun node_attr(nodeId: Int, attr: String): String {
        val element = elements[nodeId]
        return element?.attr(attr) ?: ""
    }
}

expect object NativeBridge {
    fun request(url: String, method: Int): String
    fun initLogger(logger: Any)
    fun initNativeBridge(nativeContext: NativeContext)
    fun load(bytes: ByteArray)
    fun add(a: Int, b: Int): Int
    fun callMethod(name: String): String
}