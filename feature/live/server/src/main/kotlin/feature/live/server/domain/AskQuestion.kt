package feature.live.server.domain

import feature.live.LiveEvent
import feature.live.Question

fun interface AskQuestion {
    /**
     * Puts a question on every screen.
     *
     * @throws IllegalArgumentException when [text] is blank or too long.
     */
    suspend operator fun invoke(clientId: String, text: String): Question
}

internal class AskQuestionImpl(
    private val addQuestion: AddQuestion,
    private val publishLiveEvent: PublishLiveEvent,
) : AskQuestion {
    override suspend fun invoke(clientId: String, text: String): Question {
        require(Question.accepts(text)) {
            "Keep it between 1 and ${Question.MAX_LENGTH} characters"
        }
        val question = addQuestion(clientId, text.trim())
        publishLiveEvent(LiveEvent.QuestionAsked(question))
        return question
    }
}
