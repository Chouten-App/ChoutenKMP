package dev.chouten.core.repository

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Instant

data class SourceModule(
    val id: String,
    val binary: ByteArray? = null,
    val script: String? = null
)

enum class HttpMethod {
    GET, POST, PUT, DELETE;

    companion object {
        fun fromInt(value: Int): HttpMethod =
            entries.getOrNull(value) ?: GET
    }
}

@Serializable
data class HttpResponse(
    val statusCode: Int,
    val body: String?,
    val headers: Map<String, String>
)

interface HostEnvironment {
    /// Network functions
    fun request(url: String, method: HttpMethod): Int

    /// Response functions
    fun responseBodyAsDoc(docId: Int): Int

    /// HTML parsing functions
    fun htmlParse(html: String): Int
    fun querySelector(docId: Int, query: String): Int
    fun querySelectorAll(docId: Int, query: String): List<Int>
    fun nodeQuerySelector(nodeId: Int, query: String): Int
    fun nodeQuerySelectorAll(nodeId: Int, query: String): List<Int>
    fun nodeText(nodeId: Int): String
    fun nodeAttr(nodeId: Int, attr: String): String

    /// Storage functions
    fun storageGet(key: String): String
    fun storageSet(key: String, value: String)

    /// Debug functions
    fun log(level: String, message: String)
}


data class Log(
    val time: Instant,
    val message: String
)

@OptIn(ExperimentalAtomicApi::class)
class DefaultHostEnvironment(): HostEnvironment {
    var logs: List<Log> = listOf()

    private val nextId = AtomicInt(1)

    private val responses = mutableMapOf<Int, HttpResponse>()

    private val documents = mutableMapOf<Int, Document>()
    private val elements = mutableMapOf<Int, Element>()

    inline fun <reified T> encode(value: T): String =
        Json.encodeToString(value)
    inline fun <reified T> decode(value: String): T =
        Json.decodeFromString(value)

    override fun request(
        url: String,
        method: HttpMethod
    ): Int {
        println("request START: $url")
        val result = runBlocking {
            val response = withContext(Dispatchers.IO) {
                httpClient.request(url) {
                    // Configure your request here
                    this.method = when (method.ordinal) {
                        0 -> io.ktor.http.HttpMethod.Get
                        1 -> io.ktor.http.HttpMethod.Post
                        // Add other methods as needed
                        else -> io.ktor.http.HttpMethod.Get
                    }
                }
            }

            val httpResponse = HttpResponse(
                statusCode = response.status.value,
                body = response.bodyAsText(),
                headers = emptyMap()//response.headers.toMap(),
            )

            val id = nextId.fetchAndAdd(1)
            responses[id] = httpResponse
            id
        }
        println("request END: $url took result=$result")
        return result
    }

    override fun responseBodyAsDoc(docId: Int): Int {
        val body = responses[docId]?.body

        body?.let {
            return htmlParse(body)
        }
        return -1
    }

    override fun htmlParse(html: String): Int {
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

    override fun querySelector(
        docId: Int,
        query: String
    ): Int {
        val doc = documents[docId] ?: return -1

        val element = doc.selectFirst(query) ?: return -1

        val id = nextId.fetchAndAdd(1)
        elements[id] = element

        return id
    }

    override fun querySelectorAll(
        docId: Int,
        query: String
    ): List<Int> {
        val doc = documents[docId] ?: return emptyList()
        val matchedElements = doc.select(query)

        return matchedElements.map { element ->
            val elementId = nextId.fetchAndAdd(1)
            elements[elementId] = element
            elementId
        }
    }

    override fun nodeQuerySelector(
        nodeId: Int,
        query: String
    ): Int {
        val parent = elements[nodeId] ?: return -1

        val element = parent.selectFirst(query) ?: return -1

        val id = nextId.fetchAndAdd(1)
        elements[id] = element

        return id
    }

    override fun nodeQuerySelectorAll(
        nodeId: Int,
        query: String
    ): List<Int> {
        TODO("Not yet implemented")
    }

    override fun nodeText(nodeId: Int): String {
        val element = elements[nodeId]
        println("[nodeText] Returning ${element?.text()}")
        return element?.text() ?: ""
    }

    override fun nodeAttr(
        nodeId: Int,
        attr: String
    ): String {
        val element = elements[nodeId]
        return element?.attr(attr) ?: ""
    }

    override fun storageGet(key: String): String {
        TODO("Not yet implemented")
    }

    override fun storageSet(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun log(level: String, message: String) {
        val now = Clock.System.now()
        logs += Log(now, message)

        println("HostEnvironment -> $message")
    }

}

interface Runtime {
    suspend fun load(module: SourceModule)
    suspend fun initialize(host: HostEnvironment? = null)

    fun discover(): Result<List<DiscoverList>, ChoutenError>

    fun search(query: String, filters: List<String>): List<PosterData>
    fun supports(module: InstalledModule): Boolean
}
