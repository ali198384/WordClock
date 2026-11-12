plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.moko.resources)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.ghazarian.wordclock.core.resources"
        compileSdk = 37
        minSdk = 26
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.moko.resources)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.ghazarian.wordclock.resources")
    resourcesClassName.set("MR")
    iosBaseLocalizationRegion.set("en")
}
