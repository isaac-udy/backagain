plugins {
    id("backagain.jvm-server")
    id("backagain.dev-database")
    id("backagain.server-packaging")
    alias(libs.plugins.ktor)
}

private val projectNamespace = providers.gradleProperty("backagain.projectNamespace").get()

group = projectNamespace
version = "1.0.0"

application {
    mainClass.set("$projectNamespace.ServerKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Local defaults for `run`: a known presenter password, and the web bundle from the last
// `:app:client:web:wasmJsBrowserDistribution`. Deployed servers get both from their environment.
tasks.named<JavaExec>("run") {
    val presenterPassword = providers.environmentVariable("BACKAGAIN_PRESENTER_PASSWORD").orElse("presenter")
    val webDirectory = providers.environmentVariable("BACKAGAIN_WEB_DIR")
        .orElse(rootProject.layout.projectDirectory.dir("app/client/web/build/dist/wasmJs/productionExecutable").asFile.absolutePath)
    doFirst {
        environment("BACKAGAIN_PRESENTER_PASSWORD", presenterPassword.get())
        environment("BACKAGAIN_WEB_DIR", webDirectory.get())
    }
}

tasks.named<backagain.server.SmokeTestFatJarTask>("smokeTestFatJar") {
    environment.put("BACKAGAIN_PRESENTER_PASSWORD", "smoke-test")
}

// The Docker build context: the fat jar, the production web bundle, and the Dockerfile. Needs
// `--no-configuration-cache`, like every task that runs the web bundle's webpack build.
tasks.register<Sync>("dockerContext") {
    group = "distribution"
    description = "Stages the server jar and the web bundle for `docker build app/server/build/docker`."
    from(tasks.named("shadowJar")) { rename { "server-all.jar" } }
    from(layout.projectDirectory.file("Dockerfile"))
    from(rootProject.layout.projectDirectory.dir("app/client/web/build/dist/wasmJs/productionExecutable")) {
        into("web")
        // Every browser that runs WasmGC fetches the Brotli copies over HTTPS. The raw wasm, three
        // times the size, would only go to clients that don't: scripts, mostly.
        exclude("*.wasm", "*.map")
    }
    dependsOn(":app:client:web:wasmJsBrowserDistribution")
    into(layout.buildDirectory.dir("docker"))
}

dependencies {
    implementation(projects.feature.live.server)

    implementation(projects.platform.server.http)
    implementation(projects.platform.server.postgres)
    // The only module allowed to depend on this: it carries Zonky's embedded Postgres binaries.
    implementation(projects.platform.server.development)

    implementation(libs.logback)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverCompression)
    implementation(libs.ktor.serverForwardedHeader)
    implementation(libs.ktor.serverAutoHeadResponse)
    runtimeOnly(libs.cloudSql.postgresSocketFactory)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.koin.test)
}
