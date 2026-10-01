package feature.live.server.data

import feature.live.server.data.storage.PollVoteStorage
import feature.live.server.domain.FlowOfPollTallies
import feature.live.server.domain.GetPollTallies
import feature.live.server.domain.RecordVote
import kotlinx.coroutines.flow.mapNotNull
import platform.server.postgres.tables.PollVoteRow
import kotlin.time.Clock

internal class PollRepository(
    private val storage: PollVoteStorage,
    private val clock: Clock,
) {
    val getPollTallies = GetPollTallies {
        storage.listOptionCounts().toTallies()
    }

    val recordVote = RecordVote { clientId, pollId, optionId ->
        storage.upsert(
            PollVoteRow(
                pollId = pollId,
                clientId = clientId,
                optionId = optionId,
                votedAt = clock.now(),
            ),
        )
    }

    // A poll the deck no longer asks, still in the table, has no tally to publish.
    val flowOfPollTallies = FlowOfPollTallies {
        storage.observeChangedPolls().mapNotNull { pollId -> tallyOf(pollId) }
    }

    private suspend fun tallyOf(pollId: String) =
        storage.listOptionCounts(pollId).toTallies().firstOrNull { it.pollId == pollId }
}
