plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version Versions.composeDesktopWeb
    application
}

group = "me.joreilly"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":shared"))
}

application {
    mainClass.set("MainKt")
}

compose {
    kotlinCompilerPlugin.set("1.5.4-dev1-kt2.0.0-Beta1")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=2.0.0-Beta1")
}
