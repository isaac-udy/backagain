package feature.deck.client.ui

import feature.deck.client.domain.DeckMode
import feature.deck.client.domain.Slide
import feature.deck.client.domain.Talk
import feature.live.DeckPosition
import feature.live.Reaction
import feature.live.client.domain.LiveState

data class DeckState(
    val live: LiveState = LiveState.Initial,
    val isPresenter: Boolean = false,
    val isStage: Boolean = false,
    /** Where this screen is when it isn't following the presenter. */
    val localPosition: DeckPosition = Talk.deck.first,
    val isDetached: Boolean = false,
    val floatingReactions: List<FloatingReaction> = emptyList(),
) {
    val mode: DeckMode
        get() = when {
            isPresenter -> DeckMode.Presenting
            live.isLive && !isDetached -> DeckMode.Following
            live.isLive -> DeckMode.Detached
            else -> DeckMode.Browsing
        }

    val position: DeckPosition
        get() = Talk.deck.resolve(if (mode == DeckMode.Following) live.position else localPosition)

    val slide: Slide get() = Talk.deck.slide(position.slideId) ?: Talk.deck.slides.first()

    val slideNumber: Int get() = Talk.deck.indexOf(position) + 1

    val slideCount: Int get() = Talk.deck.slides.size

    /** One reaction drifting up the screen; [lane] is where across the screen it rises, from 0 to 1. */
    data class FloatingReaction(
        val id: Long,
        val reaction: Reaction,
        val lane: Float,
    )
}
