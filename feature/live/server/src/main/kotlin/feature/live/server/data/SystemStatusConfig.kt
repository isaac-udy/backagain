package feature.live.server.data

internal data class SystemStatusConfig(
    val revision: String,
    val gitSha: String,
    val buildTime: String,
)
