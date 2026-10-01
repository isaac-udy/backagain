package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.navigationHandle
import dev.isaacudy.udytils.coroutines.JobManager
import dev.isaacudy.udytils.state.AsyncState
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.fromSuspending
import dev.isaacudy.udytils.state.viewModelState
import feature.live.Question
import feature.live.client.domain.AskQuestion
import feature.live.client.domain.FlowOfLiveState
import kotlinx.coroutines.launch

class QuestionsSlideViewModel(
    private val flowOfLiveState: FlowOfLiveState,
    private val askQuestion: AskQuestion,
) : ViewModel() {

    private val navigation by navigationHandle<QuestionsSlideDestination>()
    private val jobManager = JobManager(viewModelScope)

    val state: ViewModelState<QuestionsSlideState> = viewModelState(QuestionsSlideState())

    init {
        viewModelScope.launch {
            flowOfLiveState().collect { live -> state.update { copy(questions = live.questions) } }
        }
    }

    fun onDraftChanged(draft: String) {
        state.update { copy(draft = draft.take(Question.MAX_LENGTH), asking = if (asking is AsyncState.Success) AsyncState.Idle() else asking) }
    }

    fun onAsk() {
        if (!state.value.canAsk) return
        val text = state.value.draft.trim()
        jobManager.launchReplacing(ASK) {
            AsyncState.fromSuspending { askQuestion(text) }.collect { asking ->
                state.update {
                    copy(
                        asking = asking,
                        draft = if (asking is AsyncState.Success) "" else draft,
                    )
                }
            }
        }
    }

    private companion object {
        const val ASK = "ask"
    }
}
