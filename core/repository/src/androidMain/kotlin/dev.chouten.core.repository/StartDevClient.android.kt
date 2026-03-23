package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

actual val httpClient: HttpClient = HttpClient(OkHttp) {
    install(WebSockets)
    install(Logging) {
        level = LogLevel.ALL
    }
}