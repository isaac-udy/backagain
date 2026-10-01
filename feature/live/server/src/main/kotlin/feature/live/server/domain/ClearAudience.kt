package feature.live.server.domain

/** Deletes every vote, question and reaction total, together. */
fun interface ClearAudience {
    suspend operator fun invoke()
}
