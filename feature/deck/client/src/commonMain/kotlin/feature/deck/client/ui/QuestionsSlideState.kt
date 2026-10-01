package feature.deck.client.ui

import dev.isaacudy.udytils.state.AsyncState
import feature.live.Question

data class QuestionsSlideState(
    /** The questions on screen, newest first. */
    val questions: List<Question> = emptyList(),
    val draft: String = "",
    val asking: AsyncState<Unit> = AsyncState.Idle(),
) {
    val canAsk: Boolean get() = Question.accepts(draft) && asking !is AsyncState.Loading
}
