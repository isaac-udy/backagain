package feature.live.server.domain

/**
 * Clears what the audience left behind, for the next time the talk is given: votes, questions,
 * reactions and the request count. Everyone following gets a fresh snapshot, as if they'd just
 * connected.
 */
fun interface ResetAudience {
    suspend operator fun invoke()
}

internal class ResetAudienceImpl(
    private val clearAudience: ClearAudience,
    private val takeQueuedReactions: TakeQueuedReactions,
    private val restartRequestCount: RestartRequestCount,
    private val getLiveSnapshot: GetLiveSnapshot,
    private val publishLiveEvent: PublishLiveEvent,
) : ResetAudience {
    override suspend fun invoke() {
        clearAudience()
        takeQueuedReactions()
        restartRequestCount()
        publishLiveEvent(getLiveSnapshot())
    }
}
