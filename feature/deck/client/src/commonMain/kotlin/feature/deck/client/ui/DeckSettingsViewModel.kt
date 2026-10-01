package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.requestClose
import dev.isaacudy.udytils.coroutines.JobManager
import dev.isaacudy.udytils.state.AsyncState
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.fromSuspending
import dev.isaacudy.udytils.state.viewModelState
import feature.deck.client.domain.FlowOfIsStage
import feature.deck.client.domain.SetStage
import feature.live.client.domain.FlowOfIsPresenter
import feature.live.client.domain.FlowOfLiveState
import feature.live.client.domain.ResetAudience
import feature.live.client.domain.SignOutPresenter
import feature.live.client.domain.UpdatePresentation
import kotlinx.coroutines.launch

class DeckSettingsViewModel(
    private val flowOfIsPresenter: FlowOfIsPresenter,
    private val flowOfLiveState: FlowOfLiveState,
    private val updatePresentation: UpdatePresentation,
    private val signOutPresenter: SignOutPresenter,
    private val resetAudience: ResetAudience,
    private val flowOfIsStage: FlowOfIsStage,
    private val setStage: SetStage,
) : ViewModel() {

    private val navigation by navigationHandle<DeckSettingsDestination>()
    private val jobManager = JobManager(viewModelScope)

    val state: ViewModelState<DeckSettingsState> = viewModelState(DeckSettingsState())

    init {
        viewModelScope.launch {
            flowOfIsPresenter().collect { isPresenter -> state.update { copy(isPresenter = isPresenter) } }
        }
        viewModelScope.launch {
            flowOfLiveState().collect { live -> state.update { copy(isLive = live.isLive) } }
        }
        viewModelScope.launch {
            flowOfIsStage().collect { isStage -> state.update { copy(isStage = isStage) } }
        }
    }

    fun onToggleStage() {
        val isStage = state.value.isStage
        viewModelScope.launch { setStage(!isStage) }
    }

    fun onToggleLive() {
        val isLive = state.value.isLive
        jobManager.launchReplacing(SET_LIVE) {
            runCatching { updatePresentation.setLive(!isLive) }
        }
    }

    /** The first call asks for confirmation; the second clears everything. */
    fun onResetAudience() {
        if (!state.value.confirmingReset) {
            state.update { copy(confirmingReset = true, resetting = AsyncState.Idle()) }
            return
        }
        jobManager.launchReplacing(RESET) {
            AsyncState.fromSuspending { resetAudience() }.collect { resetting ->
                state.update { copy(resetting = resetting, confirmingReset = resetting is AsyncState.Loading) }
            }
        }
    }

    fun onStartPresenting() {
        navigation.closeAndReplaceWith(PresenterSignInDestination)
    }

    fun onStopPresenting() {
        viewModelScope.launch {
            signOutPresenter()
            navigation.requestClose()
        }
    }

    fun onDismiss() {
        navigation.requestClose()
    }

    private companion object {
        const val SET_LIVE = "setLive"
        const val RESET = "reset"
    }
}
