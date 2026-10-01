plugins {
    id("backagain.compose-library")
    alias(libs.plugins.kotlinKsp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.deck.api)
            implementation(projects.feature.live.api)

            implementation(projects.platform.client.design)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.qrose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.enro.core)
            implementation(libs.udytils.ui)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.enro.test)
        }
    }
}

compose.resources {
    packageOfResClass = "feature.deck.client.resources"
}

dependencies {
    add("kspCommonMainMetadata", libs.enro.processor)
    add("kspJvm", libs.enro.processor)
    add("kspWasmJs", libs.enro.processor)
}
