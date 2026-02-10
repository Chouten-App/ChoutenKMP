package dev.chouten.core.repository

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual val httpClient: HttpClient = HttpClient(Darwin) {
    engine {
        configureRequest {
            timeout = 15000 // milliseconds
        }
    }
}
