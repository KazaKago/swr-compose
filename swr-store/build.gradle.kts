import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.buildLogicPublish)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "com.kazakago.swr.store"
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
            implementation(libs.kotlinxCoroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinxCoroutinesTest)
            implementation(libs.turbine)
        }
    }
}
