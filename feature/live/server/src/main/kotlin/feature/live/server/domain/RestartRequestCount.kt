package feature.live.server.domain

/** Starts [CountRequestsServed] again from zero. */
fun interface RestartRequestCount {
    suspend operator fun invoke()
}
