plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.2.0"
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
