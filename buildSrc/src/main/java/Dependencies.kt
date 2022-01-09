
object Versions {
    const val kotlinVersion = "1.6.10"
    const val kotlinCoroutines = "1.6.0"

    const val compose = "1.1.0-rc01"
    const val composeCompiler = "1.1.0-rc02"
    const val navCompose = "2.4.0-rc01"
    const val accompanist = "0.21.0-beta"
    const val okio = "3.0.0"

    const val wearCompose = "1.0.0-alpha13"
    const val composeDesktopWeb = "1.0.1"

    const val kmpNativeCoroutines = "0.11.1-new-mm"

    const val junit = "4.13"
}


object AndroidSdk {
    const val min = 24
    const val minWear = 28
    const val compile = 31
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

    const val wearFoundation = "androidx.wear.compose:compose-foundation:${Versions.wearCompose}"
    const val wearMaterial = "androidx.wear.compose:compose-material:${Versions.wearCompose}"
    const val wearNavigation = "androidx.wear.compose:compose-navigation:${Versions.wearCompose}"

    const val activityCompose = "androidx.activity:activity-compose:1.4.0"
}

object Google {
    object Accompanist {
        const val insets = "com.google.accompanist:accompanist-insets:${Versions.accompanist}"
        const val pager = "com.google.accompanist:accompanist-pager:${Versions.accompanist}"
        const val pagerIndicator = "com.google.accompanist:accompanist-pager-indicators:${Versions.accompanist}"
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







