package feature.live.server.services

import feature.live.server.domain.PublishLiveCounters
import platform.server.http.BackgroundJob
import kotlin.time.Duration.Companion.milliseconds

internal class LiveCountersJob(
    private val publishLiveCounters: PublishLiveCounters,
) : BackgroundJob {

    override val period = 250.milliseconds

    override suspend fun run() {
        publishLiveCounters()
    }
}
