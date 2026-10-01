package feature.live.client.domain

/** Presenter only: clears every vote, question and reaction, ready for the next talk. */
fun interface ResetAudience {
    suspend operator fun invoke()
}
