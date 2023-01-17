plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = 33
    defaultConfig {
        minSdk = 23
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.lifecycle:lifecycle-common:2.5.1")

    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("org.robolectric:robolectric:4.9.2")
    testImplementation("androidx.compose.ui:ui-test-junit4")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
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
            version = "0.6.0"
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
