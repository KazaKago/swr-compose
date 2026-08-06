import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.example)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinxCoroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "com.kazakago.swr.example.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kazakago.swr.example"
            packageVersion = "1.0.0"
        }
    }
}
