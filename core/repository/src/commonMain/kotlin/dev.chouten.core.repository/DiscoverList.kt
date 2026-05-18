package dev.chouten.core.repository

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverList(
    val title: String,
    val section_type: String,
    val list: List<PosterData>
)