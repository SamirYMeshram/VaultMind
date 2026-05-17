import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.vaultmind.core.database"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { jvmToolchain(17) }
}

val sqliteTmpDir = rootProject.layout.projectDirectory.dir(".gradle/tmp").asFile
sqliteTmpDir.mkdirs()
tasks.withType<KaptWithoutKotlincTask>().configureEach {
    doFirst { sqliteTmpDir.mkdirs() }
    kaptProcessJvmArgs.add("-Dorg.sqlite.tmpdir=${sqliteTmpDir.absolutePath}")
    kaptProcessJvmArgs.add("-Djava.io.tmpdir=${sqliteTmpDir.absolutePath}")
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
