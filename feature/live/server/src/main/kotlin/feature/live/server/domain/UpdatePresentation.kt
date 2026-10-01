package feature.live.server.domain

import feature.live.LiveEvent

/** Moves the deck or starts and ends the live session, and tells every follower. */
fun interface UpdatePresentation {
    suspend operator fun invoke(update: UpdateDeckState.Update)
}

internal class UpdatePresentationImpl(
    private val updateDeckState: UpdateDeckState,
    private val restartRequestCount: RestartRequestCount,
    private val publishLiveEvent: PublishLiveEvent,
) : UpdatePresentation {
    override suspend fun invoke(update: UpdateDeckState.Update) {
        val state = updateDeckState(update)
        val event = when (update) {
            is UpdateDeckState.Update.MoveTo -> LiveEvent.PositionChanged(update.position)
            is UpdateDeckState.Update.SetLive -> LiveEvent.LiveChanged(state.isLive)
        }
        // "Requests served during this talk" counts from the moment it goes live.
        if (update is UpdateDeckState.Update.SetLive && state.isLive) restartRequestCount()
        publishLiveEvent(event)
    }
}
