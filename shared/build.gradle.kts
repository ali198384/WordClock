plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}
kotlin {
    android {
        namespace = "com.ghazarian.wordclock.shared"
        compileSdk = 37
        minSdk = 26
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())
    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "WordClockShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.domain)
            api(projects.core.storage)
            implementation(projects.core.designsystem)
            implementation(projects.feature.devices)
            implementation(projects.feature.control)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.compose.runtime)
            implementation(libs.compose.components.resources)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        iosMain.dependencies {
            implementation(projects.core.data)
        }
    }
}
