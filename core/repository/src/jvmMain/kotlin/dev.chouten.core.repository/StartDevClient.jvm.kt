package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.logging.*

actual val httpClient: HttpClient = HttpClient(CIO) {
    install(WebSockets)
    install(Logging) {
        level = LogLevel.ALL
    }
}
