package platform.server.http

import io.ktor.server.routing.Route

/**
 * A feature's HTTP and WebSocket routes. The server installs every `RouteHandler` bound in Koin,
 * so a feature's routes are live once its dependency module binds the class.
 */
interface RouteHandler {

    /** Registered with Ktor's RateLimit plugin before any route is installed. */
    val rateLimits: List<RateLimitPolicy> get() = emptyList()

    fun Route.install()
}
