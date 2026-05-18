package dev.chouten.core.repository

import kotlinx.serialization.Serializable

@Serializable
data class PosterData(
    val url: String,
    val titles: Title,
    val poster: String,
    val banner: String?,
    val description: String?,
    val indicator: String?,
    val current: Int?,
    val total: Int?
)