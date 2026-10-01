package feature.live.server.services

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object PollApi {

    /** POST a [VoteRequest]. A second vote from the same client replaces the first. */
    @Serializable
    @Resource("/api/polls/{pollId}/votes")
    class Votes(val pollId: String)

    @Serializable
    data class VoteRequest(val optionId: String)
}
