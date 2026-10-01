package feature.live.client.domain

import feature.live.Reaction

fun interface SendReaction {
    suspend operator fun invoke(reaction: Reaction)
}
