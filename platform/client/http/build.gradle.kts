plugins {
    id("backagain.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.platform.common.http)

            api(libs.ktor.clientCore)
            api(libs.ktor.clientResources)
            api(libs.ktor.clientWebsockets)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.koin.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
            implementation(libs.kotlinx.browser)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientJava)
        }
    }
}
