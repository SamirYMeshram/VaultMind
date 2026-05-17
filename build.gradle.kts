plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.legacy.kapt) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}


val vaultMindBuildTmpDir = rootProject.layout.projectDirectory.dir(".gradle/tmp").asFile
vaultMindBuildTmpDir.mkdirs()
System.setProperty("org.sqlite.tmpdir", vaultMindBuildTmpDir.absolutePath)

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    tasks.configureEach {
        doFirst {
            vaultMindBuildTmpDir.mkdirs()
            System.setProperty("org.sqlite.tmpdir", vaultMindBuildTmpDir.absolutePath)
        }
    }
}
