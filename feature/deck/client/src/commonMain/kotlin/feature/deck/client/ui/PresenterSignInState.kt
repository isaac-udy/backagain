package feature.deck.client.ui

import dev.isaacudy.udytils.state.AsyncState

data class PresenterSignInState(
    val password: String = "",
    val signingIn: AsyncState<Unit> = AsyncState.Idle(),
)
