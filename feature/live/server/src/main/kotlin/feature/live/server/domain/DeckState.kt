package feature.live.server.domain

import feature.live.DeckPosition

/** [position] is null until the presenter first moves. */
data class DeckState(
    val position: DeckPosition?,
    val isLive: Boolean,
)
