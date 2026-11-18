package feature.deck.client.domain

import feature.live.LiveEvent
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TalkTest {

    @Test
    fun `the short deck is a cut of the full deck`() {
        // Talk.short throws on first access if it names a slide the full deck does not contain.
        assertTrue(Talk.short.slides.size < Talk.full.slides.size)
    }

    @Test
    fun `the full deck has the id the server reports by default`() {
        assertSame(Talk.full, Talk.decks[LiveEvent.DEFAULT_DECK_ID])
    }
}
