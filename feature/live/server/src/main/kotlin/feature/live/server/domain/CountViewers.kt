package feature.live.server.domain

/** How many clients are subscribed to live events right now. */
fun interface CountViewers {
    suspend operator fun invoke(): Int
}
