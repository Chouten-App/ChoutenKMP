package com.inumaki.core.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class Title(
    val primary: String,
    val secondary: String?
)