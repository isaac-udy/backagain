package feature.deck.client.domain

import feature.live.DeckPosition

/** The slides in order, and how "next" and "previous" move through them and their steps. */
data class Deck(
    val slides: List<Slide>,
) {
    val first: DeckPosition get() = DeckPosition(slideId = slides.first().id, step = 0)

    fun slide(id: String): Slide? = slides.firstOrNull { it.id == id }

    fun indexOf(position: DeckPosition): Int = slides.indexOfFirst { it.id == position.slideId }

    /** [position] if it names a slide in this deck, clamped to that slide's steps; otherwise [first]. */
    fun resolve(position: DeckPosition?): DeckPosition {
        val slide = position?.let { slide(it.slideId) } ?: return first
        return position.copy(step = position.step.coerceIn(0, slide.steps))
    }

    fun next(position: DeckPosition): DeckPosition? {
        val current = resolve(position)
        val index = indexOf(current)
        if (current.step < slides[index].steps) return current.copy(step = current.step + 1)
        val following = slides.getOrNull(index + 1) ?: return null
        return DeckPosition(slideId = following.id, step = 0)
    }

    /** Back one step; from a slide's first step, to the previous slide with everything revealed. */
    fun previous(position: DeckPosition): DeckPosition? {
        val current = resolve(position)
        if (current.step > 0) return current.copy(step = current.step - 1)
        val preceding = slides.getOrNull(indexOf(current) - 1) ?: return null
        return DeckPosition(slideId = preceding.id, step = preceding.steps)
    }
}
