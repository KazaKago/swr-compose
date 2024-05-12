plugins {
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.dokka")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Unit Tests
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotest.assertions)

    // UI Tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

if (project.plugins.hasPlugin("com.android.library")) {
    android.publishing.singleVariant("release") {
        withSourcesJar()
    }
} else {
    java {
        withSourcesJar()
    }
}

tasks.create("javadocJar", Jar::class) {
    group = "publishing"
    archiveClassifier.set("javadoc")
    from(tasks["dokkaHtml"])
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.kazakago.swr.compose"
            artifactId = "swr-android"
            version = "0.6.3"
            pom {
                name.set("swr-compose")
                description.set("\"React SWR\" ported for Jetpack Compose")
                url.set("https://github.com/kazakago/swr-compose")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                scm {
                    connection.set("git:git@github.com:kazakago/swr-compose")
                    developerConnection.set("git:git@github.com:kazakago/swr-compose")
                    url.set("https://github.com/kazakago/swr-compose")
                }
                developers {
                    developer {
                        name.set("kazakago")
                        email.set("kazakago@gmail.com")
                        url.set("https://blog.kazakago.com/")
                    }
                }
            }
            afterEvaluate {
                if (project.plugins.hasPlugin("com.android.library")) {
                    from(components["release"])
                } else {
                    from(components["java"])
                }
            }
            artifact(tasks["javadocJar"])
        }
    }
}

signing {
    val keyId = System.getenv("SIGNING_KEY_ID") ?: findProperty("signing.keyId").toString()
    val secretKey = System.getenv("SIGNING_SECRET_KEY") ?: findProperty("signing.secretKey").toString()
    val password = System.getenv("SIGNING_PASSWORD") ?: findProperty("signing.password").toString()
    useInMemoryPgpKeys(keyId, secretKey, password)
    sign(publishing.publications)
}
