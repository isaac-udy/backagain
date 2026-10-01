package com.isaacudy.backagain

import dev.isaacudy.udytils.postgres.PostgresConfig
import dev.isaacudy.udytils.postgres.PostgresMigrator
import dev.isaacudy.udytils.postgres.buildHikariDataSource
import dev.isaacudy.udytils.postgres.embedded.DevServer
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import platform.http.ApiJson
import platform.server.development.BackAgainDevDatabase
import platform.server.http.BackgroundJob
import platform.server.http.BandwidthBudget
import platform.server.http.RouteHandler
import platform.server.http.apiErrors
import platform.server.http.countServedRequests
import platform.server.http.register
import platform.server.http.start
import platform.server.http.webBundle
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

fun main() {
    // Resolved before the server is built: the schema has to be migrated — and a dev database
    // booted and seeded — before anything can serve.
    val postgresConfig = resolvePostgresConfig()

    embeddedServer(Netty, port = ServerConfiguration.serverPort, host = "0.0.0.0") {
        install(Koin) {
            modules(serverDependencies(postgresConfig))
        }
        module()
    }.start(wait = true)
}

private fun Application.module() {
    val routeHandlers = getKoin().getAll<RouteHandler>()

    // Cloud Run terminates TLS and forwards the request, so the scheme and client address come
    // from the X-Forwarded-* headers it adds.
    install(XForwardedHeaders)
    countServedRequests(getKoin().get())
    install(CallLogging) {
        filter { call -> call.request.path().startsWith("/api") }
    }
    install(ContentNegotiation) {
        json(ApiJson)
    }
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 30.seconds
        contentConverter = KotlinxWebsocketSerializationConverter(ApiJson)
    }
    // Brotli-encoded wasm already carries a Content-Encoding, which Compression leaves alone.
    install(Compression) {
        gzip { minimumSize(1024) }
    }
    install(StatusPages) {
        apiErrors()
    }
    install(RateLimit) {
        register(routeHandlers.flatMap { it.rateLimits })
    }
    install(Resources)
    install(AutoHeadResponse)

    routing {
        get("/healthz") { call.respond(HttpStatusCode.OK) }
        routeHandlers.forEach { handler -> with(handler) { install() } }
        route("/api/{...}") {
            handle { call.respond(HttpStatusCode.NotFound) }
        }
        ServerConfiguration.webDirectory?.let { webBundle(it, webBandwidthBudget()) }
    }

    start(getKoin().getAll<BackgroundJob>())
}

/**
 * A first visit downloads about 4 MB, so a room of a thousand uses about 4 GB, well inside the hour.
 * The daily limit is what bounds the bill: at list price, 25 GB is a few dollars.
 */
private fun webBandwidthBudget() = BandwidthBudget(
    listOf(
        BandwidthBudget.Limit(bytes = 10 * GIGABYTE, window = 1.hours),
        BandwidthBudget.Limit(bytes = 25 * GIGABYTE, window = 1.days),
    ),
)

private const val GIGABYTE = 1_000_000_000L

/**
 * The dev path starts an embedded Postgres and migrates and seeds it itself; every other path
 * points at a real server and migrates it here, before the first request can reach it.
 */
private fun resolvePostgresConfig(): PostgresConfig {
    // The deployed jar leaves the development module out, so it is only loaded when asked for.
    if (ServerConfiguration.devDatabaseMode != null) {
        startDevDatabase()?.let { return it }
    }

    val postgresConfig = ServerConfiguration.postgresConfigFromEnv
    // Its own short-lived pool, closed before Koin opens the one the server serves from.
    buildHikariDataSource(postgresConfig).use { PostgresMigrator(it).migrate() }
    return postgresConfig
}

private fun startDevDatabase(): PostgresConfig? {
    val devServerConfig = BackAgainDevDatabase.configFor(
        mode = ServerConfiguration.devDatabaseMode,
        scenarioName = ServerConfiguration.devDatabaseScenario,
        baseDirectory = ServerConfiguration.devDatabaseDirectory,
    ) ?: return null
    return DevServer.start(devServerConfig).postgresConfig
}
