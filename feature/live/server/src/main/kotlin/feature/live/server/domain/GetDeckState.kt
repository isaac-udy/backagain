package feature.live.server.domain

fun interface GetDeckState {
    suspend operator fun invoke(): DeckState
}
