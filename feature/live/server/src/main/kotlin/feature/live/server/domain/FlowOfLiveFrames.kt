package feature.live.server.domain

import feature.live.LiveFrame
import kotlinx.coroutines.flow.Flow

/** What one live socket sends: a snapshot of everything, then every change after it. */
fun interface FlowOfLiveFrames {
    operator fun invoke(): Flow<LiveFrame>
}

internal class FlowOfLiveFramesImpl(
    private val flowOfPublishedFrames: FlowOfPublishedFrames,
    private val getLiveSnapshot: GetLiveSnapshot,
) : FlowOfLiveFrames {
    override fun invoke(): Flow<LiveFrame> = flowOfPublishedFrames { getLiveSnapshot() }
}
