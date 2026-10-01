package feature.live.server.domain

fun interface RecordVote {
    /** Saves the vote, replacing the client's earlier one; the new tally arrives from [FlowOfPollTallies]. */
    suspend operator fun invoke(clientId: String, pollId: String, optionId: String)
}
