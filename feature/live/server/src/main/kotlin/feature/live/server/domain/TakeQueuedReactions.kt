package feature.live.server.domain

import feature.live.Reaction

fun interface TakeQueuedReactions {
    /** Empties the queue [SendReaction] fills, returning how many of each reaction it held. */
    suspend operator fun invoke(): Map<Reaction, Int>
}
