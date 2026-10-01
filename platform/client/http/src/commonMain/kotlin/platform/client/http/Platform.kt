package platform.client.http

import io.ktor.client.engine.HttpClientEngine

internal expect fun platformHttpEngine(): HttpClientEngine

internal expect fun currentOrigin(): String

internal expect fun loadOrCreateClientId(): ClientId
