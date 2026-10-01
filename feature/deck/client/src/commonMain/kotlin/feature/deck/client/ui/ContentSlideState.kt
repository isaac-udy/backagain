package feature.deck.client.ui

import feature.live.client.domain.LiveState

data class ContentSlideState(
    val slideId: String,
    val live: LiveState = LiveState.Initial,
)
