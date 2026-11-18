package feature.deck.client.ui

import feature.deck.client.domain.Deck
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
    val localPosition: DeckPosition = Talk.full.first,
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

    /** The deck the presenter has chosen, for everyone; the full talk for an id this client doesn't know. */
    val deck: Deck get() = Talk.decks[live.deckId] ?: Talk.full

    val position: DeckPosition
        get() = deck.resolve(if (mode == DeckMode.Following) live.position else localPosition)

    val slide: Slide get() = deck.slide(position.slideId) ?: deck.slides.first()

    val slideNumber: Int get() = deck.indexOf(position) + 1

    val slideCount: Int get() = deck.slides.size

    /** One reaction drifting up the screen; [lane] is where across the screen it rises, from 0 to 1. */
    data class FloatingReaction(
        val id: Long,
        val reaction: Reaction,
        val lane: Float,
    )
}
