package feature.deck.client.ui

import dev.isaacudy.udytils.state.AsyncState
import feature.live.Poll
import feature.live.PollTally

data class PollSlideState(
    val poll: Poll,
    val tally: PollTally,
    val slideId: String = "",
    val showsJoinCode: Boolean = false,
    /** The slide step that reveals the results; until then, or with none, it only counts the votes. */
    val resultsFromStep: Int? = 0,
    /** The option this browser voted for; the server keeps only the latest vote per client. */
    val myVote: String? = null,
    val voting: AsyncState<Unit> = AsyncState.Idle(),
)
