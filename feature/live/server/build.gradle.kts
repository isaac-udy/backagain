plugins {
    id("backagain.jvm-library")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

dependencies {
    api(projects.feature.live.api)

    implementation(projects.platform.server.http)
    implementation(projects.platform.server.postgres)

    implementation(libs.ktor.serverWebsockets)
    implementation(libs.koin.core)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutinesTest)
}
