package feature.live.server.domain

import feature.live.LiveEvent

/** Everything a follower needs to draw the deck from scratch. */
fun interface GetLiveSnapshot {
    suspend operator fun invoke(): LiveEvent.Snapshot
}

internal class GetLiveSnapshotImpl(
    private val getDeckState: GetDeckState,
    private val getQuestionsOnScreen: GetQuestionsOnScreen,
    private val getPollTallies: GetPollTallies,
    private val getReactionTotals: GetReactionTotals,
    private val countViewers: CountViewers,
    private val getSystemStatus: GetSystemStatus,
    private val countRequestsServed: CountRequestsServed,
) : GetLiveSnapshot {

    private val questionsShown = 50

    override suspend fun invoke(): LiveEvent.Snapshot {
        val deck = getDeckState()
        return LiveEvent.Snapshot(
            position = deck.position,
            isLive = deck.isLive,
            questions = getQuestionsOnScreen(limit = questionsShown),
            tallies = getPollTallies(),
            reactionTotals = getReactionTotals(),
            viewers = countViewers(),
            system = getSystemStatus(),
            requestsServed = countRequestsServed(),
        )
    }
}
