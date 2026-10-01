package feature.live.client.domain

fun interface CastVote {
    suspend operator fun invoke(pollId: String, optionId: String)
}
