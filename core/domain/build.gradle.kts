plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.ghazarian.wordclock.core.domain"
        compileSdk = 37
        minSdk = 26
        withHostTest {}
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(projects.core.testing)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
