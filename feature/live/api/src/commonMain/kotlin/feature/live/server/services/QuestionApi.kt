package feature.live.server.services

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object QuestionApi {

    /** POST an [AskRequest] for the stored `feature.live.Question`, which goes straight on screen. */
    @Serializable
    @Resource("/api/questions")
    class Questions

    /** POST to take a question off screen. Presenter only. */
    @Serializable
    @Resource("/api/questions/{id}/hide")
    class Hide(val id: String)

    @Serializable
    data class AskRequest(val text: String)
}
