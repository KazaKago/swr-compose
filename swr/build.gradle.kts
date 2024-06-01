plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Unit Tests
    testImplementation(kotlin("test"))
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

group = "com.kazakago.swr.compose"
version = libs.versions.version.get()

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications.register<MavenPublication>("release") {
        artifact(javadocJar.get())
        artifactId = "swr-android"
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
            from(components["release"])
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
