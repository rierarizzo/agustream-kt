import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))
    implementation(project(":core"))
    implementation(project(":data"))

    implementation(compose.desktop.currentOs)
    implementation(compose.ui)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.ktor.clientCio)

    // Image loading: the shared UI renders through Coil, and this is where the
    // desktop ImageLoader is configured (the default decoder aliases).
    implementation(libs.coil.compose)

    // Desktop playback engine: libmpv, loaded in process through JNA and driven
    // through its client and render APIs. Isolated behind PlayerService.
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)

    implementation(libs.compose.uiToolingPreview)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
}

compose.desktop {
    application {
        mainClass = "com.misgus.agustream.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.misgus.agustream"
            packageVersion = "1.0.0"
        }
    }
}
