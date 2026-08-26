import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.buildlogic.publish)
}

kotlin {
    explicitApi()

    android {
        namespace = "com.kazakago.swr.runtime"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
            freeCompilerArgs.add("-Xjdk-release=${libs.versions.jvm.target.get()}")
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.swrStore)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.androidx.lifecycleRuntime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.androidx.lifecycleRuntimeTesting)
            implementation(libs.turbine)
        }
        androidMain.dependencies {
            implementation(libs.androidx.startup)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
