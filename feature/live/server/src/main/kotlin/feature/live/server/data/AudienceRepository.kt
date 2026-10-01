package feature.live.server.data

import feature.live.server.data.storage.PollVoteStorage
import feature.live.server.data.storage.QuestionStorage
import feature.live.server.data.storage.ReactionTotalStorage
import feature.live.server.domain.ClearAudience
import platform.server.postgres.TransactionRunner

/** Everything the audience writes, as one thing to clear between talks. */
internal class AudienceRepository(
    private val pollVotes: PollVoteStorage,
    private val questions: QuestionStorage,
    private val reactionTotals: ReactionTotalStorage,
    private val transactionRunner: TransactionRunner,
) {
    val clearAudience = ClearAudience {
        transactionRunner.inTransaction {
            pollVotes.deleteAll()
            questions.deleteAll()
            reactionTotals.deleteAll()
        }
    }
}
