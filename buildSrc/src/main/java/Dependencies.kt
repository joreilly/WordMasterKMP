
object Versions {
    const val kotlinVersion = "1.7.20"
    const val kotlinCoroutines = "1.6.4"

    const val compose = "1.3.0-rc01"
    const val composeCompiler = "1.3.2"
    const val composeDesktopWeb = "1.2.0"
    const val navCompose = "2.5.2"
    const val accompanist = "0.26.2-beta"
    const val okio = "3.0.0"

    const val kmpNativeCoroutines = "0.13.1"

    const val junit = "4.13"
}


object AndroidSdk {
    const val min = 24
    const val compile = 33
    const val target = compile
}

object Compose {
    const val compiler = "androidx.compose.compiler:compiler:${Versions.composeCompiler}"
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.navCompose}"
}

object Google {
    object Accompanist {
        const val insets = "com.google.accompanist:accompanist-insets:${Versions.accompanist}"
    }
}

object Test {
    const val junit = "junit:junit:${Versions.junit}"
    const val composeUiTest = "androidx.compose.ui:ui-test:${Versions.compose}"
    const val composeUiTestJUnit = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
    const val composeUiTestManifest = "androidx.compose.ui:ui-test-manifest:${Versions.compose}"
}

object Kotlinx {
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
}

object Square {
    const val okio = "com.squareup.okio:okio:${Versions.okio}"
}







