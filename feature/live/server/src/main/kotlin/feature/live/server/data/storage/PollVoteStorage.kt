package feature.live.server.data.storage

import dev.isaacudy.udytils.postgres.PgNotificationBus
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import platform.server.postgres.tables.PollVoteRow
import platform.server.postgres.tables.PollVotesTable
import platform.server.postgres.tables.setFromRow

internal class PollVoteStorage(
    private val database: Database,
    private val bus: PgNotificationBus,
) {
    /** The id of each poll whose votes change, as they change: `LISTEN poll_votes`. */
    fun observeChangedPolls(): Flow<String> = bus.listen(CHANNEL)

    /** Records [row], replacing the client's earlier vote in the same poll. */
    suspend fun upsert(row: PollVoteRow) {
        suspendTransaction(db = database) {
            PollVotesTable.upsert { it.setFromRow(row) }
        }
    }

    /** Every poll's trigger fires as its votes go, so followers see the tallies empty. */
    suspend fun deleteAll() {
        suspendTransaction(db = database) {
            PollVotesTable.deleteAll()
        }
    }

    /** Votes per option, for [pollId], or for every poll when it is null. */
    suspend fun listOptionCounts(pollId: String? = null): List<PollOptionCountRecord> = suspendTransaction(db = database) {
        val votes = PollVotesTable.clientId.count()
        PollVotesTable
            .select(PollVotesTable.pollId, PollVotesTable.optionId, votes)
            .apply { if (pollId != null) where { PollVotesTable.pollId eq pollId } }
            .groupBy(PollVotesTable.pollId, PollVotesTable.optionId)
            .map { row ->
                PollOptionCountRecord(
                    pollId = row[PollVotesTable.pollId],
                    optionId = row[PollVotesTable.optionId],
                    votes = row[votes].toInt(),
                )
            }
    }

    companion object {
        /** Notified by the trigger in `R__notify_triggers.sql`. */
        const val CHANNEL = "poll_votes"
    }
}
