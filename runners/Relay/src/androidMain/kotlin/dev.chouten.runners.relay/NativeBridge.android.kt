package dev.chouten.runners.relay

import dev.chouten.core.repository.httpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


actual object NativeBridge {
    init { System.loadLibrary("relay") }

    actual fun request(url: String, method: Int): String {
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

    external fun nativeLoadWasm(bytes: ByteArray)
    actual external fun initLogger(logger: Any)
    actual external fun initNativeBridge(nativeContext: NativeContext)

    actual fun load(bytes: ByteArray) {
        nativeLoadWasm(bytes)
    }
    actual external fun add(a: Int, b: Int): Int
    actual external fun callMethod(name: String): String
}