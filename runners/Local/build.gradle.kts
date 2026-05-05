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
    jvm("desktop")

    if (isMac) {
        iosArm64 {
            binaries.framework {
                baseName = "Local"
                isStatic = false
            }
        }

        iosSimulatorArm64 {
            binaries.framework {
                baseName = "Local"
                isStatic = false
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)

                implementation("io.coil-kt.coil3:coil-compose:3.3.0")
                implementation("io.coil-kt.coil3:coil-svg:3.0.4")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
                implementation("com.fleeksoft.ksoup:ksoup:0.2.6")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

                implementation(project(":core:UI"))
                implementation(project(":core:repository"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
            }
        }

        if (isMac) {
            val iosMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
                    implementation("io.ktor:ktor-client-darwin:3.1.3")
                }
            }
            val iosArm64Main by getting { dependsOn(iosMain) }
            val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        }
    }
}

android {
    namespace = "dev.chouten.runners.local"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
