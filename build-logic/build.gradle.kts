plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.dokka.plugin)
    implementation(libs.maven.publishPlugin)
}
