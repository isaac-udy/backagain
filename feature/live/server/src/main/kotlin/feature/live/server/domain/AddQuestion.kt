package feature.live.server.domain

import feature.live.Question

/** Saves a question, on screen until it's hidden. */
fun interface AddQuestion {
    suspend operator fun invoke(clientId: String, text: String): Question
}
