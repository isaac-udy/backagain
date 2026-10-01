package platform.client.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal actual fun platformHttpEngine(): HttpClientEngine = Java.create()

internal actual fun currentOrigin(): String = "http://localhost:8080"

@OptIn(ExperimentalUuidApi::class)
internal actual fun loadOrCreateClientId(): ClientId = ClientId(Uuid.random().toString())
