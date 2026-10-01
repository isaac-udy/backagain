package feature.live.client.domain

import feature.live.DeckPosition
import feature.live.PollTally
import feature.live.Question
import feature.live.Reaction
import feature.live.SystemStatus

/** Everything the live socket has told this client, as of the last frame it applied. */
data class LiveState(
    val connection: LiveConnection,
    val position: DeckPosition?,
    val isLive: Boolean,
    /** The questions on screen, newest first. */
    val questions: List<Question>,
    val tallies: Map<String, PollTally>,
    val reactionTotals: Map<Reaction, Long>,
    val viewers: Int,
    val system: SystemStatus?,
    /** Since the live session started. */
    val requestsServed: Long,
) {
    companion object {
        val Initial: LiveState = LiveState(
            connection = LiveConnection.Connecting,
            position = null,
            isLive = false,
            questions = emptyList(),
            tallies = emptyMap(),
            reactionTotals = emptyMap(),
            viewers = 0,
            system = null,
            requestsServed = 0,
        )
    }
}

enum class LiveConnection {
    Connecting,
    Connected,
    Reconnecting,
}
