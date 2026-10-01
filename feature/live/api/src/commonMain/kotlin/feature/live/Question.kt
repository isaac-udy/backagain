package feature.live

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A question from the audience, on every screen until the presenter hides it. */
@Serializable
data class Question(
    val id: String,
    val text: String,
    val askedAt: Instant,
) {
    companion object {
        const val MAX_LENGTH: Int = 140

        /** Whether [text] can be asked: checked on the phone as you type, and again by the server. */
        fun accepts(text: String): Boolean = text.trim().length in 1..MAX_LENGTH
    }
}
