plugins {
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username").toString())
            password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password").toString())
            stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.stagingProfileId").toString())
        }
    }
}
