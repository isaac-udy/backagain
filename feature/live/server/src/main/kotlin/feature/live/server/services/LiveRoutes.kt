package feature.live.server.services

import feature.live.server.domain.FlowOfLiveFrames
import io.ktor.server.routing.Route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import platform.server.http.RouteHandler

internal class LiveRoutes(
    private val flowOfLiveFrames: FlowOfLiveFrames,
) : RouteHandler {

    override fun Route.install() {
        webSocket(LiveApi.SOCKET) {
            flowOfLiveFrames().collect { frame -> sendSerialized(frame) }
        }
    }
}
