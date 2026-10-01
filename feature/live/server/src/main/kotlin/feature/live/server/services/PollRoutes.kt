package feature.live.server.services

import feature.live.server.domain.CastVote
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import platform.server.http.RateLimitPolicy
import platform.server.http.RouteHandler
import platform.server.http.clientId
import kotlin.time.Duration.Companion.minutes

internal class PollRoutes(
    private val castVote: CastVote,
) : RouteHandler {

    override val rateLimits = listOf(
        RateLimitPolicy(VOTE, limit = 20, refillPeriod = 1.minutes, scope = RateLimitPolicy.Scope.PerClient),
    )

    override fun Route.install() {
        rateLimit(VOTE) {
            post<PollApi.Votes> { votes ->
                val request = call.receive<PollApi.VoteRequest>()
                castVote(call.clientId(), votes.pollId, request.optionId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

    private companion object {
        val VOTE = RateLimitName("poll-vote")
    }
}
