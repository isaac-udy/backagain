package feature.live.server.domain

import feature.live.LiveEvent
import feature.live.LiveFrame
import kotlinx.coroutines.flow.Flow

fun interface FlowOfPublishedFrames {
    /**
     * Every event published from now on, preceded by [snapshot]. The snapshot's frame carries the
     * sequence number of the last event it already includes, so the subscriber can tell which of
     * the frames that follow it are new.
     */
    operator fun invoke(snapshot: suspend () -> LiveEvent.Snapshot): Flow<LiveFrame>
}
