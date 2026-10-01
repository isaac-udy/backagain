package feature.live.server.data

import feature.live.DeckPosition
import feature.live.PollTally
import feature.live.Polls
import feature.live.Question
import feature.live.Reaction
import feature.live.server.data.storage.PollOptionCountRecord
import feature.live.server.domain.DeckState
import platform.server.postgres.tables.DeckStateRow
import platform.server.postgres.tables.QuestionRow
import platform.server.postgres.tables.ReactionTotalRow

internal fun DeckStateRow?.toDomain(): DeckState = DeckState(
    position = this?.let { DeckPosition(slideId = it.slideId, step = it.step) },
    isLive = this?.isLive ?: false,
)

internal fun QuestionRow.toDomain(): Question = Question(
    id = id.toString(),
    text = text,
    askedAt = askedAt,
)

/** One tally per known poll, so a poll nobody has voted in still has every option at zero. */
internal fun List<PollOptionCountRecord>.toTallies(): List<PollTally> =
    Polls.all.map { poll ->
        val counts = filter { it.pollId == poll.id }.associate { it.optionId to it.votes }
        PollTally(
            pollId = poll.id,
            votes = poll.options.associate { option -> option.id to (counts[option.id] ?: 0) },
        )
    }

internal fun List<ReactionTotalRow>.toTotals(): Map<Reaction, Long> {
    val stored = associate { it.reaction to it.total }
    return Reaction.entries.associateWith { stored[it.name] ?: 0L }
}
