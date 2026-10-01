package feature.live.client.data

import feature.live.Reaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * This device's reactions, shown the moment they're tapped rather than a round trip later. The
 * server's bursts include them too, so each tap is held until a burst carrying one like it
 * arrives, and is taken back out of that burst. Reactions of a kind look alike, so it only
 * matters how many are taken out, not whose.
 *
 * A tap that hasn't come back within a few seconds is given up on, so a request the server dropped
 * doesn't go on hiding someone else's reaction later.
 */
internal class SentReactions(
    private val clock: Clock,
) {
    private val expiry = 5.seconds
    private val mutex = Mutex()
    private val awaiting = mutableListOf<Pair<Reaction, Instant>>()

    val echoes: Flow<Map<Reaction, Int>>
        field = MutableSharedFlow<Map<Reaction, Int>>(extraBufferCapacity = 16)

    suspend fun sent(reaction: Reaction) {
        mutex.withLock { awaiting += reaction to clock.now() }
        echoes.emit(mapOf(reaction to 1))
    }

    /** The server never took it, so no burst will carry it back. */
    suspend fun failed(reaction: Reaction) {
        mutex.withLock {
            val index = awaiting.indexOfFirst { it.first == reaction }
            if (index >= 0) awaiting.removeAt(index)
        }
    }

    /** [burst] without the taps this device has already shown. */
    suspend fun withoutOwn(burst: Map<Reaction, Int>): Map<Reaction, Int> = mutex.withLock {
        val now = clock.now()
        awaiting.removeAll { (_, sentAt) -> now - sentAt > expiry }
        burst
            .mapValues { (reaction, count) ->
                var remaining = count
                while (remaining > 0) {
                    val index = awaiting.indexOfFirst { it.first == reaction }
                    if (index < 0) break
                    awaiting.removeAt(index)
                    remaining -= 1
                }
                remaining
            }
            .filterValues { it > 0 }
    }
}
