package dev.chouten.core.repository

import kotlinx.serialization.Serializable

@Serializable
data class Title(
    val primary: String,
    val secondary: String?
)