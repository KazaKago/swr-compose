import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "com.kazakago.swr.example"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        androidResources.enable = true
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Example"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.swrCompose)
            implementation(libs.composeMaterial3)
            implementation(libs.composeUiToolingPreview)
            implementation(libs.composeResources)
            implementation(libs.kotlinxDatetime)
            implementation(libs.kotlinxSerializationCore)
            implementation(libs.androidxNavigation3Ui)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.composeUiTooling)
}
