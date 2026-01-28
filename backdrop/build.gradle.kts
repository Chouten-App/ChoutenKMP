import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


val os = System.getProperty("os.name")
val arch = System.getProperty("os.arch")

val isLinuxArm = os == "Linux" && (arch == "aarch64" || arch == "arm64")
val isMac = os == "Mac OS X"

kotlin {
    androidLibrary {
        namespace = "com.kyant.backdrop"
        compileSdk = 36
        buildToolsVersion = "36.1.0"
        minSdk = 21

        withJava()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    if (isMac) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Backdrop"
                isStatic = true
            }
        }
    }
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.compose.ui.graphics)
        }
        val skikoMain by creating {
            dependsOn(commonMain.get())
        }
        iosMain.get().dependsOn(skikoMain)
        jvmMain.get().dependsOn(skikoMain)
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xexpect-actual-classes"
        )
    }
}
