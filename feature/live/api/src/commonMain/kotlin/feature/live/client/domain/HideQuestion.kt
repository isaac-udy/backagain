package feature.live.client.domain

/** Presenter only: takes a question off screen, or out of the queue. */
fun interface HideQuestion {
    suspend operator fun invoke(id: String)
}
