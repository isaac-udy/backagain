package feature.live.client.domain

import feature.live.LiveEvent

/** The state after [event]. Every event carries absolute values, so this never accumulates drift. */
internal fun LiveState.applying(event: LiveEvent): LiveState = when (event) {
    is LiveEvent.Snapshot -> copy(
        connection = LiveConnection.Connected,
        position = event.position,
        isLive = event.isLive,
        questions = event.questions,
        tallies = event.tallies.associateBy { it.pollId },
        reactionTotals = event.reactionTotals,
        viewers = event.viewers,
        system = event.system,
        requestsServed = event.requestsServed,
    )
    is LiveEvent.PositionChanged -> copy(position = event.position)
    is LiveEvent.LiveChanged -> copy(isLive = event.isLive)
    is LiveEvent.QuestionAsked -> copy(
        questions = (listOf(event.question) + questions.filterNot { it.id == event.question.id }).take(QUESTIONS_KEPT),
    )
    is LiveEvent.QuestionHidden -> copy(questions = questions.filterNot { it.id == event.id })
    is LiveEvent.PollTallied -> copy(tallies = tallies + (event.tally.pollId to event.tally))
    is LiveEvent.ReactionsBurst -> copy(reactionTotals = event.totals)
    is LiveEvent.ViewersChanged -> copy(viewers = event.viewers)
    is LiveEvent.RequestsServedChanged -> copy(requestsServed = event.requestsServed)
}

private const val QUESTIONS_KEPT = 50
