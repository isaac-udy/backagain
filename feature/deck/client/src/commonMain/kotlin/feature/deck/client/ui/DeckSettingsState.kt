package feature.deck.client.ui

import dev.isaacudy.udytils.state.AsyncState
import feature.deck.client.domain.Deck
import feature.deck.client.domain.Talk

data class DeckSettingsState(
    val isPresenter: Boolean = false,
    val isLive: Boolean = false,
    val isStage: Boolean = false,
    val deck: Deck = Talk.full,
    /** The reset has been asked for once, and needs asking again before it happens. */
    val confirmingReset: Boolean = false,
    val resetting: AsyncState<Unit> = AsyncState.Idle(),
)
