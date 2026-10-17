plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl::class)
    export {
        swift {
            moduleName = "shared"
            rootPackage = "dev.johnoreilly.wordmaster.shared"
            xcodeIntegration()
        }
    }

    android {
        namespace = "dev.johnoreilly.wordmaster.shared"

        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.okio)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}


