package feature.live.client.data.storage

import feature.live.Polls
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal actual class VoteStorage actual constructor() {

    actual val votes: StateFlow<Map<String, String>>
        field = MutableStateFlow(
            Polls.all.mapNotNull { poll -> localStorage.getItem(key(poll.id))?.let { poll.id to it } }.toMap(),
        )

    actual fun setVote(pollId: String, optionId: String) {
        localStorage.setItem(key(pollId), optionId)
        votes.update { it + (pollId to optionId) }
    }

    private fun key(pollId: String) = "backagain.vote.$pollId"
}
