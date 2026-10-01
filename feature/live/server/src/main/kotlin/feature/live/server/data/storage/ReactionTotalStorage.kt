package feature.live.server.data.storage

import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import platform.server.postgres.tables.ReactionTotalRow
import platform.server.postgres.tables.ReactionTotalsTable

internal class ReactionTotalStorage(
    private val database: Database,
) {
    suspend fun list(): List<ReactionTotalRow> = suspendTransaction(db = database) {
        ReactionTotalsTable.selectAll().map(::ReactionTotalRow)
    }

    /** Adds each count to its reaction's running total, in one transaction. */
    suspend fun upsertIncrements(increments: List<ReactionTotalRow>) {
        suspendTransaction(db = database) {
            increments.forEach { increment ->
                ReactionTotalsTable.upsert(
                    onUpdate = { it[ReactionTotalsTable.total] = ReactionTotalsTable.total + increment.total },
                ) {
                    it[ReactionTotalsTable.reaction] = increment.reaction
                    it[ReactionTotalsTable.total] = increment.total
                }
            }
        }
    }

    suspend fun deleteAll() {
        suspendTransaction(db = database) {
            ReactionTotalsTable.deleteAll()
        }
    }
}
