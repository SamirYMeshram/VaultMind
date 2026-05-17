plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.vaultmind.core.datastore"
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
kapt { correctErrorTypes = true }

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
