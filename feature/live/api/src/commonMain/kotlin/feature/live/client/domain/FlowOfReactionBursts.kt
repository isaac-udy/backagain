package feature.live.client.domain

import feature.live.Reaction
import kotlinx.coroutines.flow.Flow

/**
 * Reactions as they arrive, a batch at a time, for animating; this device's own arrive the moment
 * they're sent. The running totals are in [LiveState].
 */
fun interface FlowOfReactionBursts {
    operator fun invoke(): Flow<Map<Reaction, Int>>
}
