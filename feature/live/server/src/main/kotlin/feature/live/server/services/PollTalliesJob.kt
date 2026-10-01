package feature.live.server.services

import feature.live.server.domain.PublishPollTallies
import platform.server.http.BackgroundJob
import kotlin.time.Duration.Companion.seconds

/** Publishes tallies for as long as the server runs; [period] is only the pause before a restart. */
internal class PollTalliesJob(
    private val publishPollTallies: PublishPollTallies,
) : BackgroundJob {

    override val period = 1.seconds

    override suspend fun run() {
        publishPollTallies()
    }
}
