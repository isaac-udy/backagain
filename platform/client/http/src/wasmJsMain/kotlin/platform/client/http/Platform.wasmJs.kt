package platform.client.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val CLIENT_ID_KEY = "backagain.client-id"

internal actual fun platformHttpEngine(): HttpClientEngine = Js.create()

internal actual fun currentOrigin(): String = window.location.origin

@OptIn(ExperimentalUuidApi::class)
internal actual fun loadOrCreateClientId(): ClientId {
    val existing = localStorage.getItem(CLIENT_ID_KEY)
    if (existing != null) return ClientId(existing)
    val created = Uuid.random().toString()
    localStorage.setItem(CLIENT_ID_KEY, created)
    return ClientId(created)
}
