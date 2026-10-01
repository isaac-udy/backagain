plugins {
    id("backagain.compose-library")
    alias(libs.plugins.kotlinKsp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.deck.client)
            implementation(projects.feature.live.client)
            implementation(projects.platform.client.http)

            // The design system: App() installs BackAgainTheme once, above navigation.
            implementation(projects.platform.client.design)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Exposed as `api` because this module's public API surface includes Enro types
            // (the @NavigationComponent object and the generated installNavigationController).
            api(libs.enro.core)
            implementation(libs.udytils.ui)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.enro.processor)
    add("kspJvm", libs.enro.processor)
    add("kspWasmJs", libs.enro.processor)
}
