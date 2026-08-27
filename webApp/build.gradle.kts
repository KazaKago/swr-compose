import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    js {
        browser()
        binaries.executable()
    }
    sourceSets {
        webMain.dependencies {
            implementation(projects.example)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}
