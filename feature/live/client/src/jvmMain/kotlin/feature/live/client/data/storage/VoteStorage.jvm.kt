package feature.live.client.data.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal actual class VoteStorage actual constructor() {

    actual val votes: StateFlow<Map<String, String>>
        field = MutableStateFlow(emptyMap())

    actual fun setVote(pollId: String, optionId: String) {
        votes.update { it + (pollId to optionId) }
    }
}
