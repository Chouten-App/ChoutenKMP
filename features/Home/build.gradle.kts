plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val os = System.getProperty("os.name")
val arch = System.getProperty("os.arch")

val isLinuxArm = os == "Linux" && (arch == "aarch64" || arch == "arm64")
val isMac = os == "Mac OS X"

kotlin {
    androidTarget()
    jvm()

    if (isMac) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Home"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")

            api(project(":core:UI"))
            implementation(project(":runners:Relay"))
        }

        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.0")
        }


        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "dev.chouten.features.home"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
