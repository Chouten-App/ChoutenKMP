package com.inumaki.core.ui.model

interface DevClient {
    suspend fun sendLog(message: String)
}
