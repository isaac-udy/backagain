package feature.live.server.domain

import feature.live.LiveEvent
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Publishes the reactions queued, the viewer count and the requests served, each when it has
 * changed since the last call. Every request changes the last, so it goes at most every
 * [REQUESTS_EVERY] calls rather than on each one.
 */
fun interface PublishLiveCounters {
    suspend operator fun invoke()
}

internal class PublishLiveCountersImpl(
    private val takeQueuedReactions: TakeQueuedReactions,
    private val addReactions: AddReactions,
    private val countViewers: CountViewers,
    private val countRequestsServed: CountRequestsServed,
    private val publishLiveEvent: PublishLiveEvent,
) : PublishLiveCounters {

    private val lastPublishedViewers = AtomicInteger(-1)
    private val lastPublishedRequests = AtomicLong(-1)
    private val calls = AtomicInteger()

    override suspend fun invoke() {
        val reactions = takeQueuedReactions()
        if (reactions.isNotEmpty()) {
            val totals = addReactions(reactions)
            publishLiveEvent(LiveEvent.ReactionsBurst(counts = reactions, totals = totals))
        }
        val viewers = countViewers()
        if (lastPublishedViewers.getAndSet(viewers) != viewers) {
            publishLiveEvent(LiveEvent.ViewersChanged(viewers))
        }
        if (calls.incrementAndGet() % REQUESTS_EVERY == 0) {
            val requests = countRequestsServed()
            if (lastPublishedRequests.getAndSet(requests) != requests) {
                publishLiveEvent(LiveEvent.RequestsServedChanged(requests))
            }
        }
    }

    private companion object {
        const val REQUESTS_EVERY = 4
    }
}
