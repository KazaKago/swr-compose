plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.dokka")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

kotlin {
    jvmToolchain(17)
    explicitApi()
    androidTarget()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtimeSaveable)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }
        commonTest.dependencies {
            implementation("io.kotest:kotest-assertions-core:5.5.4")
        }
        androidMain.dependencies {
            implementation(compose.ui)
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(project.dependencies.platform("androidx.compose:compose-bom:2023.10.01"))
                implementation("androidx.compose.ui:ui-test-junit4")
                implementation("androidx.compose.ui:ui-test-manifest")
                implementation("org.robolectric:robolectric:4.11.1")
            }
        }
    }
}

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
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
