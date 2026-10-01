package feature.live.server.domain

import feature.live.LiveEvent

/** Takes a question off every screen. */
fun interface HideQuestion {
    suspend operator fun invoke(id: String)
}

internal class HideQuestionImpl(
    private val markQuestionHidden: MarkQuestionHidden,
    private val publishLiveEvent: PublishLiveEvent,
) : HideQuestion {
    override suspend fun invoke(id: String) {
        if (!markQuestionHidden(id)) return
        publishLiveEvent(LiveEvent.QuestionHidden(id))
    }
}
