plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "me.joreilly"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":shared"))
}

application {
    mainClass.set("MainKt")
}

