package feature.deck.client.domain

import kotlinx.coroutines.flow.StateFlow

/** Whether this screen is the stage: the projector, showing the deck and the room's reactions, and no controls. */
fun interface FlowOfIsStage {
    operator fun invoke(): StateFlow<Boolean>
}
