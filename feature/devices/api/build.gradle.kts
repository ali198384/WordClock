plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ghazarian.wordclock.feature.devices.api"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(projects.core.model)
}
