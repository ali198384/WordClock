plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ghazarian.wordclock.core.navigation"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.feature.devices.api)
    api(projects.feature.control.api)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.navigation.compose)
}
