plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.moko.resources)
}

kotlin {
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "WordClockShared"
            isStatic = true
            export(projects.core.model)
            export(projects.core.resources)
            export(libs.moko.resources)
        }
    }

    sourceSets {
        iosMain.dependencies {
            api(projects.core.model)
            api(projects.core.resources)
            api(libs.moko.resources)
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.storage)
            implementation(projects.core.data)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.ghazarian.wordclock.shared.resources")
    resourcesClassName.set("SharedMR")
    iosBaseLocalizationRegion.set("en")
}
