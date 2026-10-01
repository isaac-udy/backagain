package feature.live.server.services

import feature.live.server.domain.SendReaction
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import platform.server.http.RateLimitPolicy
import platform.server.http.RouteHandler
import kotlin.time.Duration.Companion.seconds

internal class ReactionRoutes(
    private val sendReaction: SendReaction,
) : RouteHandler {

    override val rateLimits = listOf(
        RateLimitPolicy(REACT, limit = 10, refillPeriod = 2.seconds, scope = RateLimitPolicy.Scope.PerClient),
    )

    override fun Route.install() {
        rateLimit(REACT) {
            post<ReactionApi.Reactions> {
                sendReaction(call.receive<ReactionApi.ReactionRequest>().reaction)
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }

    private companion object {
        val REACT = RateLimitName("reaction")
    }
}
