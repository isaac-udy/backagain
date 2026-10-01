package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.navigationHandle
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.viewModelState
import feature.live.client.domain.FlowOfLiveState
import kotlinx.coroutines.launch

class ContentSlideViewModel(
    private val flowOfLiveState: FlowOfLiveState,
) : ViewModel() {

    private val navigation by navigationHandle<ContentSlideDestination>()

    val state: ViewModelState<ContentSlideState> = viewModelState(ContentSlideState(slideId = navigation.key.slideId))

    init {
        viewModelScope.launch {
            flowOfLiveState().collect { live -> state.update { copy(live = live) } }
        }
    }
}
