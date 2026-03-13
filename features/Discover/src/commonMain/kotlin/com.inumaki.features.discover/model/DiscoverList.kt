package com.inumaki.features.discover.model

import com.inumaki.core.ui.model.PosterData
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverList(
    val title: String,
    val section_type: String,
    val list: List<PosterData>
)
