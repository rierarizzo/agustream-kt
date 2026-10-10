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
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.ktor.clientCio)

    // Desktop playback engine (libvlc via VLCJ). Isolated behind PlayerService.
    implementation(libs.vlcj)
    implementation(libs.vlcj.natives)
    implementation(libs.logback)

    implementation(libs.compose.uiToolingPreview)
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
