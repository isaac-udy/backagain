package platform.http

import kotlinx.serialization.json.Json

/**
 * The one JSON configuration both sides of the wire use, for request and response bodies and
 * WebSocket frames alike.
 *
 * Unknown keys are ignored so the server can add a field before every open browser tab has
 * reloaded onto a client that knows it.
 */
val ApiJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
