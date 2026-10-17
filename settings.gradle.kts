pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        // Kotlin dev builds (2.5.0-Beta2-*) for Swift export; drop once Beta2 is on Maven Central
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
    }
}



rootProject.name = "WordMasterKMP"
include(":androidApp", ":shared", "compose-desktop")

