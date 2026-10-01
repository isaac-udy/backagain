package feature.live

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** What is serving this page: the Cloud Run revision, and the commit and build that produced it. */
@Serializable
data class SystemStatus(
    val revision: String,
    val gitSha: String,
    val buildTime: String,
    val startedAt: Instant,
)
