package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.navigationHandle
import dev.isaacudy.udytils.coroutines.JobManager
import dev.isaacudy.udytils.state.AsyncState
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.fromSuspending
import dev.isaacudy.udytils.state.viewModelState
import feature.live.PollTally
import feature.live.Polls
import feature.live.client.domain.CastVote
import feature.live.client.domain.FlowOfLiveState
import feature.live.client.domain.FlowOfMyVotes
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PollSlideViewModel(
    private val flowOfLiveState: FlowOfLiveState,
    private val flowOfMyVotes: FlowOfMyVotes,
    private val castVote: CastVote,
) : ViewModel() {

    private val navigation by navigationHandle<PollSlideDestination>()
    private val jobManager = JobManager(viewModelScope)
    private val poll = Polls.all.first { it.id == navigation.key.pollId }

    val state: ViewModelState<PollSlideState> = viewModelState(
        PollSlideState(
            poll = poll,
            tally = PollTally(pollId = poll.id, votes = emptyMap()),
            slideId = navigation.key.slideId,
            showsJoinCode = navigation.key.showsJoinCode,
            resultsFromStep = navigation.key.resultsFromStep,
        ),
    )

    init {
        viewModelScope.launch {
            combine(flowOfLiveState(), flowOfMyVotes()) { live, myVotes -> live.tallies[poll.id] to myVotes[poll.id] }
                .collect { (tally, myVote) ->
                    state.update {
                        // An empty poll has been reset, and no longer holds this browser's vote.
                        val current = tally ?: this.tally
                        copy(tally = current, myVote = myVote.takeIf { current.total > 0 })
                    }
                }
        }
    }

    fun onVote(optionId: String) {
        jobManager.launchReplacing(VOTE) {
            AsyncState.fromSuspending { castVote(poll.id, optionId) }.collect { voting ->
                state.update { copy(voting = voting) }
            }
        }
    }

    private companion object {
        const val VOTE = "vote"
    }
}
