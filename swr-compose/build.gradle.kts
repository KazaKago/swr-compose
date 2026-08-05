import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildLogicPublish)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "com.kazakago.swr.compose"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        withHostTest {}
    }

    iosArm64()
    iosSimulatorArm64()

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
            api(projects.swrRuntime)
            implementation(libs.composeUi)
            implementation(libs.kotlinxCoroutinesCore)
            implementation(libs.androidxLifecycleRuntimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}
