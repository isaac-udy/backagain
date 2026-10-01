package feature.live.server.domain

import feature.live.DeckPosition

fun interface UpdateDeckState {
    suspend operator fun invoke(update: Update): DeckState

    sealed interface Update {
        data class MoveTo(val position: DeckPosition) : Update
        data class SetLive(val isLive: Boolean) : Update
    }
}
