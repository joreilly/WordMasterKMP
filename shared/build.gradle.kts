plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.rickclephas.kmp.nativecoroutines")
}

kotlin {
    android()
    jvm()

    val iosArm64 = iosArm64()
    val iosX64 = iosX64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(Kotlinx.coroutinesCore) {
                    isForce = true
                }

                implementation(Square.okio)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(Kotlinx.coroutinesTest)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
        }
        val appleTest by creating {
            dependsOn(commonTest)
        }

        val jvmMain by getting

        listOf(
            iosArm64, iosX64, iosSimulatorArm64
        ).forEach {
            it.binaries.framework {
                baseName = "shared"
            }
            getByName("${it.targetName}Main") {
                dependsOn(appleMain)
            }
            getByName("${it.targetName}Test") {
                dependsOn(appleTest)
            }
        }

    }
}

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
    }

}

