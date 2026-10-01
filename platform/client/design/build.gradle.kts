plugins {
    id("backagain.compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `api` for anything whose types appear in the token surface: BackAgainColors exposes
            // Color, BackAgainTypography exposes TextStyle, and callers construct both.
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)

            // material3 is an implementation detail *while primitives are built from foundation*:
            // BackAgainTheme wraps a MaterialTheme so raw material internals (text-field decoration,
            // dividers, LocalContentColor) inherit the tokens, but no BackAgain* type exposes a
            // material3 type.
            implementation(libs.compose.material3)

            // Bundled design-system assets (fonts, drawables) live in commonMain/composeResources.
            implementation(libs.compose.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    packageOfResClass = "platform.design.resources"
}
