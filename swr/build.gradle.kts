plugins {
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = 34
    defaultConfig {
        minSdk = 23
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Unit Tests
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.robolectric:robolectric:4.12.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")

    // UI Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
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
