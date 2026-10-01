plugins {
    id("backagain.jvm-library")
}

dependencies {
    api(projects.platform.common.http)

    api(libs.ktor.serverCore)
    api(libs.ktor.serverResources)
    api(libs.ktor.serverRateLimit)
    implementation(libs.ktor.serverStatusPages)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
