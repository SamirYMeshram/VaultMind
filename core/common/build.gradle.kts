plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.vaultmind.core.common"
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
    implementation(libs.coroutines.android)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
