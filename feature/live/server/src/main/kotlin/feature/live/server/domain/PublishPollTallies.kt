package feature.live.server.domain

import feature.live.LiveEvent

/** Tells every follower each poll's new tally as Postgres announces it. Runs until cancelled. */
fun interface PublishPollTallies {
    suspend operator fun invoke()
}

internal class PublishPollTalliesImpl(
    private val flowOfPollTallies: FlowOfPollTallies,
    private val publishLiveEvent: PublishLiveEvent,
) : PublishPollTallies {
    override suspend fun invoke() {
        flowOfPollTallies().collect { tally ->
            publishLiveEvent(LiveEvent.PollTallied(tally))
        }
    }
}
