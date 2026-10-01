package feature.live

import kotlinx.serialization.Serializable

/**
 * One message on the live socket. [seq] increases by one per event the server publishes, so a
 * client that sees a gap knows it missed something and reconnects for a fresh snapshot.
 */
@Serializable
data class LiveFrame(
    val seq: Long,
    val event: LiveEvent,
)
