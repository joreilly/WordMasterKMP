plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "dev.johnoreilly.wordmaster.androidApp"

    compileSdk = AndroidSdk.compile
    defaultConfig {
        //applicationId = "dev.johnoreilly.wordle.androidApp"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target

        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}



dependencies {
    implementation(project(":shared"))

    with (Compose) {
        implementation(compiler)
        implementation(ui)
        implementation(uiGraphics)
        implementation(uiTooling)
        implementation(foundationLayout)
        implementation(material)
        implementation(navigation)
    }

    with(Test) {
        testImplementation(junit)
        androidTestImplementation(composeUiTest)
        androidTestImplementation(composeUiTestJUnit)
        debugImplementation(composeUiTestManifest)
    }

    implementation(Google.Accompanist.insets)
}
