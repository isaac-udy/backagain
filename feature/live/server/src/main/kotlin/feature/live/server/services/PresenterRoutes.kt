package feature.live.server.services

import feature.live.DeckPosition
import feature.live.server.domain.IsPresenter
import feature.live.server.domain.ResetAudience
import feature.live.server.domain.SignInPresenter
import feature.live.server.domain.SignOutPresenter
import feature.live.server.domain.UpdateDeckState
import feature.live.server.domain.UpdatePresentation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import platform.server.http.ApiException
import platform.server.http.RateLimitPolicy
import platform.server.http.RouteHandler
import platform.server.http.bearerToken
import kotlin.time.Duration.Companion.minutes

internal class PresenterRoutes(
    private val signInPresenter: SignInPresenter,
    private val signOutPresenter: SignOutPresenter,
    private val isPresenter: IsPresenter,
    private val updatePresentation: UpdatePresentation,
    private val resetAudience: ResetAudience,
) : RouteHandler {

    override val rateLimits = listOf(
        RateLimitPolicy(SIGN_IN, limit = 5, refillPeriod = 1.minutes, scope = RateLimitPolicy.Scope.PerClient),
        // The client id is whatever the caller sends, so a guesser could mint a fresh one per try.
        RateLimitPolicy(SIGN_IN_ANYONE, limit = 30, refillPeriod = 1.minutes, scope = RateLimitPolicy.Scope.Global),
    )

    override fun Route.install() {
        rateLimit(SIGN_IN_ANYONE) {
            rateLimit(SIGN_IN) {
                post<PresenterApi.Session> {
                    val request = call.receive<PresenterApi.SignInRequest>()
                    val token = signInPresenter(request.password)
                        ?: throw ApiException.unauthorized("That password isn't right")
                    call.respond(PresenterApi.SignInResponse(token))
                }
            }
        }

        delete<PresenterApi.Session> {
            call.bearerToken()?.let { signOutPresenter(it) }
            call.respond(HttpStatusCode.NoContent)
        }

        post<PresenterApi.Position> {
            call.requirePresenter()
            updatePresentation(UpdateDeckState.Update.MoveTo(call.receive<DeckPosition>()))
            call.respond(HttpStatusCode.NoContent)
        }

        post<PresenterApi.Live> {
            call.requirePresenter()
            val request = call.receive<PresenterApi.SetLiveRequest>()
            updatePresentation(UpdateDeckState.Update.SetLive(request.isLive))
            call.respond(HttpStatusCode.NoContent)
        }

        post<PresenterApi.Reset> {
            call.requirePresenter()
            resetAudience()
            call.respond(HttpStatusCode.NoContent)
        }
    }

    private suspend fun ApplicationCall.requirePresenter() {
        if (!isPresenter(bearerToken())) throw ApiException.unauthorized("Presenters only")
    }

    private companion object {
        val SIGN_IN = RateLimitName("presenter-sign-in")
        val SIGN_IN_ANYONE = RateLimitName("presenter-sign-in-anyone")
    }
}
