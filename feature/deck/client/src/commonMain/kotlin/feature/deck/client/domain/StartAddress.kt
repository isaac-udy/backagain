package feature.deck.client.domain

/** What the address the page was opened at asks for. */
data class StartAddress(
    /** The slide in a `/s/{slideId}` address. */
    val slideId: String?,
    /** `/present`: open the presenter sign-in. */
    val wantsToPresent: Boolean,
)
