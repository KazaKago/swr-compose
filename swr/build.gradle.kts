import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

kotlin {
    explicitApi()
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        publishLibraryVariants("release")
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.konnection)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
                implementation(libs.robolectric)
                implementation(libs.mockk)
            }
        }
    }
}

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

group = "com.kazakago.swr.compose"
version = libs.versions.version.get()

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

// Workaround for JavadocJar signing issue: https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar.get())
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
    }
}

signing {
    val keyId = System.getenv("SIGNING_KEY_ID") ?: findProperty("signing.keyId").toString()
    val secretKey = System.getenv("SIGNING_SECRET_KEY") ?: findProperty("signing.secretKey").toString()
    val password = System.getenv("SIGNING_PASSWORD") ?: findProperty("signing.password").toString()
    useInMemoryPgpKeys(keyId, secretKey, password)
    sign(publishing.publications)
}
