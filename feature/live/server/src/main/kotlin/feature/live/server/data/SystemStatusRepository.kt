package feature.live.server.data

import feature.live.SystemStatus
import feature.live.server.domain.GetSystemStatus
import kotlin.time.Clock

internal class SystemStatusRepository(
    private val config: SystemStatusConfig,
    private val clock: Clock,
) {
    private val startedAt = clock.now()

    val getSystemStatus = GetSystemStatus {
        SystemStatus(
            revision = config.revision,
            gitSha = config.gitSha,
            buildTime = config.buildTime,
            startedAt = startedAt,
        )
    }
}
