plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.ghazarian.wordclock.core.designsystem"
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.ghazarian.wordclock.core.designsystem.resources"
    generateResClass = always
}
