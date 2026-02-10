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
                baseName = "CoreRepository"
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

            implementation(project.dependencies.platform("io.ktor:ktor-bom:3.4.0"))

            implementation("io.ktor:ktor-client-core")
            implementation("io.ktor:ktor-client-logging")
            implementation("io.ktor:ktor-client-content-negotiation")
            implementation("io.ktor:ktor-client-websockets")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp")
        }


        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("io.ktor:ktor-client-cio")
        }

        if (isMac) {
            iosMain.dependencies {
                implementation("io.ktor:ktor-client-darwin")
            }
        }
    }
}

android {
    namespace = "dev.chouten.core.repository"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
dependencies {
    implementation("io.ktor:ktor-client-cio-jvm:3.3.0")
}
