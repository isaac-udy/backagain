package feature.live.server.domain

fun interface MarkQuestionHidden {
    /** @return false when no question on screen has [id]. */
    suspend operator fun invoke(id: String): Boolean
}
