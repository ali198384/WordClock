plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "WordClockShared"
            isStatic = true
            export(projects.core.model)
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(projects.core.common)
            api(projects.core.model)
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.storage)
            implementation(projects.core.data)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
