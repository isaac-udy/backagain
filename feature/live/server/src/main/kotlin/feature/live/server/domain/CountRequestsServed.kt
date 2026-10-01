package feature.live.server.domain

/** How many requests the server has answered since the live session started. */
fun interface CountRequestsServed {
    suspend operator fun invoke(): Long
}
