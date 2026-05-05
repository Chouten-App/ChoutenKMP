package dev.chouten.core.repository

interface DevClient {
    suspend fun sendLog(message: String)
}