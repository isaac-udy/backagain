package feature.live

import kotlinx.serialization.Serializable

@Serializable
data class PollTally(
    val pollId: String,
    val votes: Map<String, Int>,
) {
    val total: Int get() = votes.values.sum()
}
