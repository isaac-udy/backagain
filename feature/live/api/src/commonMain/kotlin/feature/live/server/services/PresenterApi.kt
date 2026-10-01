package feature.live.server.services

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object PresenterApi {

    /** POST a [SignInRequest] for a [SignInResponse]; DELETE to sign out. */
    @Serializable
    @Resource("/api/presenter/session")
    class Session

    /** POST a `feature.live.DeckPosition` to move every follower there. */
    @Serializable
    @Resource("/api/presenter/position")
    class Position

    /** POST a [SetLiveRequest] to start or end the live session. */
    @Serializable
    @Resource("/api/presenter/live")
    class Live

    /**
     * POST to clear what the audience left behind: votes, questions and reactions. For between
     * talks; every follower gets a fresh snapshot.
     */
    @Serializable
    @Resource("/api/presenter/reset")
    class Reset

    @Serializable
    data class SignInRequest(val password: String)

    @Serializable
    data class SignInResponse(val token: String)

    @Serializable
    data class SetLiveRequest(val isLive: Boolean)
}
