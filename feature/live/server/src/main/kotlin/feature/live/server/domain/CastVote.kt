package feature.live.server.domain

import feature.live.Polls

fun interface CastVote {
    /** @throws IllegalArgumentException when the poll doesn't exist, or doesn't accept the option. */
    suspend operator fun invoke(clientId: String, pollId: String, optionId: String)
}

internal class CastVoteImpl(
    private val recordVote: RecordVote,
) : CastVote {
    override suspend fun invoke(clientId: String, pollId: String, optionId: String) {
        val poll = Polls.all.firstOrNull { it.id == pollId }
        require(poll != null && poll.accepts(optionId)) {
            "That isn't one of the options"
        }
        recordVote(clientId, pollId, optionId)
    }
}
