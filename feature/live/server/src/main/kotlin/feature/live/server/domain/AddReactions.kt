package feature.live.server.domain

import feature.live.Reaction

fun interface AddReactions {
    /** @return every reaction's running total, with [counts] added. */
    suspend operator fun invoke(counts: Map<Reaction, Int>): Map<Reaction, Long>
}
