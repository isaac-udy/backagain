package feature.deck.client.ui

import dev.isaacudy.udytils.state.AsyncState

data class DeckSettingsState(
    val isPresenter: Boolean = false,
    val isLive: Boolean = false,
    val isStage: Boolean = false,
    /** The reset has been asked for once, and needs asking again before it happens. */
    val confirmingReset: Boolean = false,
    val resetting: AsyncState<Unit> = AsyncState.Idle(),
)
