package feature.live.server.services

/**
 * The live socket: read-only, server to client. Every message is a `feature.live.LiveFrame`.
 * Clients change things with one-shot requests to the other Api contracts, and see the result
 * arrive here like everyone else does.
 */
object LiveApi {
    const val SOCKET = "/ws/live"
}
