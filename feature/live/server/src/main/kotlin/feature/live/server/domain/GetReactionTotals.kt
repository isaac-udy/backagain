package feature.live.server.domain

import feature.live.Reaction

fun interface GetReactionTotals {
    suspend operator fun invoke(): Map<Reaction, Long>
}
