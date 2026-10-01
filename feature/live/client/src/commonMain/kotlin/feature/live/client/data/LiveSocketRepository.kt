package feature.live.client.data

import feature.live.LiveEvent
import feature.live.LiveFrame
import feature.live.Reaction
import feature.live.client.domain.FlowOfLiveState
import feature.live.client.domain.FlowOfReactionBursts
import feature.live.client.domain.LiveConnection
import feature.live.client.domain.LiveState
import feature.live.client.domain.applying
import feature.live.server.services.LiveApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import platform.client.http.ApiEndpoint
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holds the live socket open while anything is watching, and folds its frames into a [LiveState].
 *
 * When the socket drops — a deploy, a Cloud Run request timeout, a phone changing networks — it
 * reconnects with exponential backoff plus jitter, so a room full of clients does not reconnect
 * in the same instant. Every connection starts with a fresh snapshot, so nothing missed while
 * disconnected is lost.
 */
internal class LiveSocketRepository(
    private val httpClient: HttpClient,
    private val endpoint: ApiEndpoint,
    private val scope: CoroutineScope,
    private val sentReactions: SentReactions,
) {
    private val bursts = MutableSharedFlow<Map<Reaction, Int>>(extraBufferCapacity = 16)

    private val liveState: StateFlow<LiveState> = channelFlow {
        var state = LiveState.Initial
        var failures = 0
        while (true) {
            try {
                httpClient.webSocket(urlString = endpoint.socketOrigin + LiveApi.SOCKET) {
                    failures = 0
                    var lastSeq = Long.MIN_VALUE
                    while (true) {
                        val frame = receiveDeserialized<LiveFrame>()
                        if (frame.event !is LiveEvent.Snapshot) {
                            if (frame.seq <= lastSeq) continue
                            if (frame.seq != lastSeq + 1) throw MissedFramesException()
                        }
                        lastSeq = frame.seq
                        state = state.applying(frame.event)
                        emitBurst(frame.event)
                        send(state)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                failures += 1
            }
            state = state.copy(connection = LiveConnection.Reconnecting)
            send(state)
            delay(backoff(failures))
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), LiveState.Initial)

    val flowOfLiveState = FlowOfLiveState { liveState }

    val flowOfReactionBursts = FlowOfReactionBursts { merge(sentReactions.echoes, bursts) }

    private suspend fun emitBurst(event: LiveEvent) {
        if (event !is LiveEvent.ReactionsBurst) return
        val others = sentReactions.withoutOwn(event.counts)
        if (others.isNotEmpty()) bursts.emit(others)
    }

    private fun backoff(failures: Int) =
        min(500L shl min(failures, 5), 15_000L).milliseconds + Random.nextLong(3_000).milliseconds

    private class MissedFramesException : RuntimeException()
}
