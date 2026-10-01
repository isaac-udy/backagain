package feature.deck.client.domain

import feature.live.DeckPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeckTest {

    private val deck = Deck(
        slides = listOf(
            Slide(id = "a", title = "A", kind = Slide.Kind.Content),
            Slide(id = "b", title = "B", kind = Slide.Kind.Content, steps = 2),
            Slide(id = "c", title = "C", kind = Slide.Kind.Content),
        ),
    )

    @Test
    fun `next reveals a slide's steps before moving on`() {
        assertEquals(DeckPosition("b", 0), deck.next(DeckPosition("a", 0)))
        assertEquals(DeckPosition("b", 1), deck.next(DeckPosition("b", 0)))
        assertEquals(DeckPosition("b", 2), deck.next(DeckPosition("b", 1)))
        assertEquals(DeckPosition("c", 0), deck.next(DeckPosition("b", 2)))
        assertNull(deck.next(DeckPosition("c", 0)))
    }

    @Test
    fun `previous goes back to the previous slide fully revealed`() {
        assertEquals(DeckPosition("b", 2), deck.previous(DeckPosition("c", 0)))
        assertEquals(DeckPosition("b", 1), deck.previous(DeckPosition("b", 2)))
        assertNull(deck.previous(DeckPosition("a", 0)))
    }

    @Test
    fun `an unknown or out of range position resolves to something showable`() {
        assertEquals(DeckPosition("a", 0), deck.resolve(null))
        assertEquals(DeckPosition("a", 0), deck.resolve(DeckPosition("removed-slide", 3)))
        assertEquals(DeckPosition("b", 2), deck.resolve(DeckPosition("b", 9)))
    }
}
