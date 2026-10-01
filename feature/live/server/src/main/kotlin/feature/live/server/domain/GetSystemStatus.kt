package feature.live.server.domain

import feature.live.SystemStatus

fun interface GetSystemStatus {
    suspend operator fun invoke(): SystemStatus
}
