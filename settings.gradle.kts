pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}



rootProject.name = "WordMasterKMP"
include(":androidApp", ":shared", "compose-desktop")

