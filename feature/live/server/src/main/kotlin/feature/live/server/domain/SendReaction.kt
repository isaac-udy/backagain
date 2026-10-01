package feature.live.server.domain

import feature.live.Reaction

/**
 * Queues a reaction for the next burst. Reactions are batched rather than broadcast one by one: a
 * room tapping at once would otherwise send every client one frame per tap, per person.
 */
fun interface SendReaction {
    suspend operator fun invoke(reaction: Reaction)
}
