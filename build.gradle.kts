plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmpNativeCoroutines) apply false
}

//buildscript {
//    repositories {
//        gradlePluginPortal()
//        google()
//        mavenCentral()
//        maven(url = "https://androidx.dev/storage/compose-compiler/repository")
//    }
//
//    dependencies {
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
//        classpath("com.android.tools.build:gradle:8.2.0")
//        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.0-Beta1-1.0.15")
//        classpath("com.rickclephas.kmp:kmp-nativecoroutines-gradle-plugin:${Versions.kmpNativeCoroutines}")
//    }
//}
//
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
//        maven(url = "https://androidx.dev/storage/compose-compiler/repository")
//    }
//}
