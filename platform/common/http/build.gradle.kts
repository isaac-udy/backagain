plugins {
    id("backagain.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization)
        }
    }
}
