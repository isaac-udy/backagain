package feature.deck.client.domain

/** Puts the slide's address in the address bar, as a step the browser's back button can return to. */
fun interface ShowSlideAddress {
    suspend operator fun invoke(slideId: String)
}
