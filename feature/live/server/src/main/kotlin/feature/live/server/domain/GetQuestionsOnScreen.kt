package feature.live.server.domain

import feature.live.Question

/** The questions the presenter hasn't hidden, newest first. */
fun interface GetQuestionsOnScreen {
    suspend operator fun invoke(limit: Int): List<Question>
}
