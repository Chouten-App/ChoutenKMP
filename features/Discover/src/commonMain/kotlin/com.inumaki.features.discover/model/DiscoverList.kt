package com.inumaki.features.discover.model

import com.inumaki.core.ui.model.PosterData

data class DiscoverList(
    val title: String,
    val type: String,
    val list: List<PosterData>
)
