package com.inumaki.core.ui.model


data class AppSettings(
    val appearanceSettings: AppearanceSettings,
    val developerSettings: DeveloperSettings,
)

data class AppearanceSettings(
    val theme: ThemePreference,
    val blurEnabled: Boolean,
    val liquidGlassEnabled: Boolean,
    val motionLevel: MotionLevel
)

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

enum class MotionLevel {
    REDUCED, NORMAL, ENHANCED
}

data class DeveloperSettings(
    val logging: Boolean,
    val cli: String,
)