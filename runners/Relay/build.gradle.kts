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

// Find cmake executable - required for building native relay library
val cmakePath: String = if (isMac) {
    listOf(
        "/opt/homebrew/bin/cmake",
        "/usr/local/bin/cmake",
        "/Applications/CMake.app/Contents/bin/cmake"
    ).firstOrNull { File(it).exists() }
        ?: error("CMake not found. Install with: brew install cmake")
} else {
    "cmake" // Linux/Windows: cmake should be in PATH
}

kotlin {
    androidTarget()
    jvm("desktop")

    if (isMac) {
        iosArm64 {
            binaries.framework {
                baseName = "Relay"
                isStatic = false
                // Native linking happens in composeApp (the final binary), not here
            }
            compilations.getByName("main") {
                cinterops {
                    val relay by creating {
                        defFile(project.file("src/nativeInterop/cinterop/relay.def"))
                        packageName("relay")
                        includeDirs(project.file("src/main/include"))
                        extraOpts("-libraryPath", project.file("build/ios-arm64").absolutePath)
                    }
                }
            }
        }

        iosSimulatorArm64 {
            binaries.framework {
                baseName = "Relay"
                isStatic = false
                // Native linking happens in composeApp (the final binary), not here
            }
            compilations.getByName("main") {
                cinterops {
                    val relay by creating {
                        defFile(project.file("src/nativeInterop/cinterop/relay.def"))
                        packageName("relay")
                        includeDirs(project.file("src/main/include"))
                        extraOpts("-libraryPath", project.file("build/ios-simulator-arm64").absolutePath)
                    }
                }
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

                implementation(project(":core:UI"))
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

tasks.matching { it.name.startsWith("cinteropRelay") }.configureEach {
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("configureRelayDesktop") {
    workingDir = file("src/main")
    commandLine(
        cmakePath,
        "-Bbuild/desktop",
        "-S.",
        "-DCMAKE_BUILD_TYPE=Debug"
    )
}

tasks.register<Exec>("compileRelayDesktop") {
    dependsOn("configureRelayDesktop")
    workingDir = file("src/main/build/desktop")
    commandLine(cmakePath, "--build", ".")
}

tasks.register("buildRelayDesktop") {
    dependsOn("compileRelayDesktop")
}

// iOS Native Build Tasks
if (isMac) {
    val srcDirPath = file("src/main").absolutePath

    fun generateCMakeLists(srcDir: String) = """
        cmake_minimum_required(VERSION 3.18)
        project(relay_ios C CXX OBJCXX)

        set(CMAKE_C_STANDARD 11)
        set(CMAKE_CXX_STANDARD 17)

        set(SRC_DIR "$srcDir")

        file(GLOB WASM3_SRC "${'$'}{SRC_DIR}/wasm3/*.c")

        add_library(relay STATIC
            ${'$'}{WASM3_SRC}
            ${'$'}{SRC_DIR}/engine/relay_native.cpp
            ${'$'}{SRC_DIR}/platform/ios/ios_bridge.mm
        )

        target_include_directories(relay PUBLIC
            ${'$'}{SRC_DIR}/include
            ${'$'}{SRC_DIR}/engine
            ${'$'}{SRC_DIR}/wasm3
        )

        target_compile_options(relay PRIVATE
            -fPIC
            -fexceptions
            -Wno-extern-c-compat
        )

        set_source_files_properties(
            ${'$'}{SRC_DIR}/platform/ios/ios_bridge.mm
            PROPERTIES COMPILE_FLAGS "-fobjc-arc"
        )
    """.trimIndent()

    data class IosVariant(
        val name: String,
        val sysroot: String,
        val target: String,
    )

    val variants = listOf(
        IosVariant("IosArm64",          "iphoneos",         "arm64-apple-ios16.0"),
        IosVariant("IosSimulatorArm64", "iphonesimulator",  "arm64-apple-ios16.0-simulator"),
    )

    for (variant in variants) {
        val buildDir = file("build/${variant.name.replace(Regex("([A-Z])"), "-$1").lowercase().trimStart('-')}")
        val cmakeFile = buildDir.resolve("CMakeLists.txt")

        tasks.register<Delete>("cleanRelay${variant.name}") {
            delete(buildDir)
        }

        tasks.register("generateCMake${variant.name}") {
            dependsOn("cleanRelay${variant.name}")
            outputs.upToDateWhen { false }
            doLast {
                buildDir.mkdirs()
                cmakeFile.writeText(generateCMakeLists(srcDirPath))
            }
        }

        tasks.register<Exec>("configureRelay${variant.name}") {
            dependsOn("generateCMake${variant.name}")
            workingDir = buildDir
            commandLine(
                cmakePath,
                "-S.", "-B.",
                "-DCMAKE_SYSTEM_NAME=iOS",
                "-DCMAKE_OSX_SYSROOT=${variant.sysroot}",
                "-DCMAKE_OSX_ARCHITECTURES=arm64",
                "-DCMAKE_BUILD_TYPE=Release",
                "-DCMAKE_C_FLAGS=-target ${variant.target}",
                "-DCMAKE_CXX_FLAGS=-target ${variant.target}"
            )
            outputs.upToDateWhen { false }
        }

        tasks.register<Exec>("compileRelay${variant.name}") {
            dependsOn("configureRelay${variant.name}")
            workingDir = buildDir
            commandLine(cmakePath, "--build", ".")
            outputs.upToDateWhen { false }
        }

        tasks.register("buildRelay${variant.name}") {
            dependsOn("compileRelay${variant.name}")
        }

        tasks.matching { it.name == "cinteropRelay${variant.name}" }.configureEach {
            dependsOn("buildRelay${variant.name}")
        }
    }

    tasks.register("buildRelayIos") {
        dependsOn(variants.map { "buildRelay${it.name}" })
    }
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
                cppFlags += "-std=c++17 -fno-limit-debug-info -fexceptions"
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
