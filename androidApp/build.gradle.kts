plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.kazakago.swr.example.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.kazakago.swr.example"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility(libs.versions.jvm.target.get())
        targetCompatibility(libs.versions.jvm.target.get())
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.example)
    implementation(libs.androidx.activityCompose)
}
