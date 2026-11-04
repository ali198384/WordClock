pluginManagement {
    repositories {
        maven { url = uri("https://maven.myket.ir/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.myket.ir/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "WordClock"
include(
    ":app",
    ":shared",
    ":core:common",
    ":core:model",
    ":core:domain",
    ":core:testing",
    ":core:network",
    ":core:storage",
    ":core:data",
    ":core:designsystem",
    ":feature:devices",
    ":feature:control",
)
 
