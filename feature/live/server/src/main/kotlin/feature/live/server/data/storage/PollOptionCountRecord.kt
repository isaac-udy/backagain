package feature.live.server.data.storage

internal data class PollOptionCountRecord(
    val pollId: String,
    val optionId: String,
    val votes: Int,
)
