package feature.live.server.services

import feature.live.server.domain.AskQuestion
import feature.live.server.domain.HideQuestion
import feature.live.server.domain.IsPresenter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import platform.server.http.ApiException
import platform.server.http.RateLimitPolicy
import platform.server.http.RouteHandler
import platform.server.http.bearerToken
import platform.server.http.clientId
import kotlin.time.Duration.Companion.seconds

internal class QuestionRoutes(
    private val askQuestion: AskQuestion,
    private val hideQuestion: HideQuestion,
    private val isPresenter: IsPresenter,
) : RouteHandler {

    override val rateLimits = listOf(
        RateLimitPolicy(ASK, limit = 2, refillPeriod = 20.seconds, scope = RateLimitPolicy.Scope.PerClient),
    )

    override fun Route.install() {
        rateLimit(ASK) {
            post<QuestionApi.Questions> {
                val request = call.receive<QuestionApi.AskRequest>()
                call.respond(askQuestion(call.clientId(), request.text))
            }
        }

        post<QuestionApi.Hide> { hide ->
            call.requirePresenter()
            hideQuestion(hide.id)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    private suspend fun ApplicationCall.requirePresenter() {
        if (!isPresenter(bearerToken())) throw ApiException.unauthorized("Presenters only")
    }

    private companion object {
        val ASK = RateLimitName("question-ask")
    }
}
