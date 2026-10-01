package feature.live.client.domain

import feature.live.DeckPosition

/** Presenter only: changes what every follower sees. */
fun interface UpdatePresentation {
    suspend operator fun invoke(update: Update)

    suspend fun moveTo(position: DeckPosition) = invoke(Update.MoveTo(position))

    suspend fun setLive(isLive: Boolean) = invoke(Update.SetLive(isLive))

    sealed interface Update {
        data class MoveTo(val position: DeckPosition) : Update
        data class SetLive(val isLive: Boolean) : Update
    }
}
