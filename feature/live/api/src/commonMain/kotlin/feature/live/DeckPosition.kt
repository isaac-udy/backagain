package feature.live

import kotlinx.serialization.Serializable

/** Where the presenter is: a slide, and how many of its build steps have been revealed. */
@Serializable
data class DeckPosition(
    val slideId: String,
    val step: Int,
)
