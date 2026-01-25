package com.inumaki.core.ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class PresentationStyle {
    Fullscreen,
    Sheet,
    Dialog
}

@Serializable
sealed interface AppRoute

fun AppRoute.presentationStyle(): PresentationStyle = when(this) {
    is SettingsRoute -> PresentationStyle.Sheet
    is InfoRoute -> PresentationStyle.Dialog
    else -> PresentationStyle.Fullscreen
}

@Serializable
@SerialName("Discover")
data object DiscoverRoute: AppRoute

@Serializable
@SerialName("Repo")
data object RepoRoute: AppRoute

@Serializable
@SerialName("Home")
data object HomeRoute: AppRoute

@Serializable
@SerialName("Info")
data object InfoRoute: AppRoute

@Serializable
@SerialName("Settings")
data object SettingsRoute: AppRoute
