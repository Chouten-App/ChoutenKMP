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
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Relay"
                isStatic = true
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
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.register<Exec>("configureRelayDesktop") {
    workingDir = file("src/main")
    commandLine(
        "cmake",
        "-Bbuild/desktop",
        "-S.",
        "-DCMAKE_BUILD_TYPE=Debug"
    )
}

tasks.register<Exec>("compileRelayDesktop") {
    dependsOn("configureRelayDesktop")
    workingDir = file("src/main/build/desktop")
    commandLine("cmake", "--build", ".")
}

tasks.register("buildRelayDesktop") {
    dependsOn("compileRelayDesktop")
}

android {
    namespace = "dev.chouten.runners.relay"
    compileSdk = 34
    ndkVersion = "27.3.13750724"

    defaultConfig {
        minSdk = 26

        ndk {
            // Only the ABIs you really need for now
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                // safer flags
                arguments += "-DANDROID_STL=c++_shared"
                cppFlags += "-std=c++17 -fno-limit-debug-info"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
