plugins {
    id("backagain.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.live.api)

            implementation(projects.platform.client.http)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
