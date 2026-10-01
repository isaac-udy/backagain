package feature.live.client.data.storage

import kotlinx.coroutines.flow.StateFlow

/**
 * This browser's vote in each poll, by poll id, kept across reloads: the client id the server
 * counts votes by survives a reload, so the vote it last counted does too.
 */
internal expect class VoteStorage() {
    val votes: StateFlow<Map<String, String>>

    fun setVote(pollId: String, optionId: String)
}
