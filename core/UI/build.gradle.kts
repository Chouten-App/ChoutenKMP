plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
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
                baseName = "CoreUI"
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

            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-svg:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
        }

        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.0")
            implementation("io.ktor:ktor-client-okhttp:3.4.0")
        }


        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation("io.ktor:ktor-client-java:3.4.0")
        }

        if (isMac) {
            iosMain.dependencies {
                implementation("io.ktor:ktor-client-darwin:3.4.0")
            }
        }
    }
}

android {
    namespace = "dev.chouten.core.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
