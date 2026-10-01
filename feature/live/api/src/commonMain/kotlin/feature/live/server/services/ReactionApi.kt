package feature.live.server.services

import feature.live.Reaction
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object ReactionApi {

    /** POST a [ReactionRequest]. */
    @Serializable
    @Resource("/api/reactions")
    class Reactions

    @Serializable
    data class ReactionRequest(val reaction: Reaction)
}
