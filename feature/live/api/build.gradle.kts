plugins {
    id("backagain.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutinesCore)
            api(libs.kotlinx.serialization)
            api(libs.ktor.resources)
        }
    }
}
