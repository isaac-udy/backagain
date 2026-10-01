package feature.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Everything the live socket tells a client. A connection opens with a [Snapshot]; every event
 * after it carries the new absolute state of what it changed, so applying one never depends on
 * having applied the one before.
 */
@Serializable
sealed interface LiveEvent {

    @Serializable
    @SerialName("LiveEvent.Snapshot")
    data class Snapshot(
        val position: DeckPosition?,
        val isLive: Boolean,
        /** The questions on screen, newest first. */
        val questions: List<Question>,
        val tallies: List<PollTally>,
        val reactionTotals: Map<Reaction, Long>,
        val viewers: Int,
        val system: SystemStatus,
        val requestsServed: Long = 0,
    ) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.PositionChanged")
    data class PositionChanged(val position: DeckPosition) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.LiveChanged")
    data class LiveChanged(val isLive: Boolean) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.QuestionAsked")
    data class QuestionAsked(val question: Question) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.QuestionHidden")
    data class QuestionHidden(val id: String) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.PollTallied")
    data class PollTallied(val tally: PollTally) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.ReactionsBurst")
    data class ReactionsBurst(
        val counts: Map<Reaction, Int>,
        val totals: Map<Reaction, Long>,
    ) : LiveEvent

    @Serializable
    @SerialName("LiveEvent.ViewersChanged")
    data class ViewersChanged(val viewers: Int) : LiveEvent

    /** How many requests the server has answered since the live session started. */
    @Serializable
    @SerialName("LiveEvent.RequestsServedChanged")
    data class RequestsServedChanged(val requestsServed: Long) : LiveEvent
}
