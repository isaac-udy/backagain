package feature.deck.client.domain

/**
 * One slide. [steps] is how many times "next" reveals something on this slide before moving to
 * the next one; a slide with nothing to reveal has none. A slide that [growsFromPrevious] carries
 * a shared element over from the slide before it, so moving between the two fades rather than
 * slides, leaving the shared element as the only thing that moves.
 */
data class Slide(
    val id: String,
    val title: String,
    val kind: Kind,
    val steps: Int = 0,
    val notes: String = "",
    val growsFromPrevious: Boolean = false,
) {
    sealed interface Kind {
        data object Content : Kind
        data object Questions : Kind

        /**
         * A poll to vote in. Until the slide's step reaches [resultsFromStep], it shows how many have
         * voted, not how; with no [resultsFromStep], it never does. One asked while people are still
         * joining [showsJoinCode].
         */
        data class Poll(
            val pollId: String,
            val showsJoinCode: Boolean = false,
            val resultsFromStep: Int? = 0,
        ) : Kind
    }
}
