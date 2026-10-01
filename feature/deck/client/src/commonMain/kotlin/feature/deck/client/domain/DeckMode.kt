package feature.deck.client.domain

enum class DeckMode {
    /** Signed in: moving the deck moves everyone following. */
    Presenting,

    /** A talk is live and this screen shows whatever the presenter shows. */
    Following,

    /** A talk is live, but this viewer has wandered off to look at something else. */
    Detached,

    /** No talk is live: the deck is free to browse. */
    Browsing,
}
