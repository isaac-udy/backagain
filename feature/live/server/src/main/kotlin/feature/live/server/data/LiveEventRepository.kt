package feature.live.server.data

import feature.live.LiveFrame
import feature.live.Reaction
import feature.live.server.domain.CountViewers
import feature.live.server.domain.FlowOfPublishedFrames
import feature.live.server.domain.PublishLiveEvent
import feature.live.server.domain.SendReaction
import feature.live.server.domain.TakeQueuedReactions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * The in-memory fan-out behind every live socket. It lives in this one process, which is why the
 * service runs as a single Cloud Run instance: a second instance would have a hub of its own, and
 * its subscribers would miss everything published on the first.
 */
internal class LiveEventRepository {

    private val frames = MutableSharedFlow<LiveFrame>(
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val publishLock = Mutex()
    private var lastSeq = 0L
    private val viewers = AtomicInteger()
    private val queuedReactions = ConcurrentHashMap<Reaction, AtomicInteger>()

    val publishLiveEvent = PublishLiveEvent { event ->
        publishLock.withLock {
            lastSeq += 1
            frames.emit(LiveFrame(seq = lastSeq, event = event))
        }
    }

    // The snapshot is taken under the publish lock, after this subscriber is already listening: an
    // event published before it is in the snapshot and has a seq at or below the snapshot's, and
    // one published after it is delivered with a higher seq. Nothing falls between the two.
    val flowOfPublishedFrames = FlowOfPublishedFrames { snapshot ->
        frames
            .onSubscription {
                val first = publishLock.withLock { LiveFrame(seq = lastSeq, event = snapshot()) }
                emit(first)
            }
            .onStart { viewers.incrementAndGet() }
            .onCompletion { viewers.decrementAndGet() }
    }

    val countViewers = CountViewers { viewers.toInt() }

    val sendReaction = SendReaction { reaction ->
        queuedReactions.computeIfAbsent(reaction) { AtomicInteger() }.incrementAndGet()
    }

    val takeQueuedReactions = TakeQueuedReactions {
        queuedReactions
            .mapValues { (_, count) -> count.getAndSet(0) }
            .filterValues { it > 0 }
    }
}
