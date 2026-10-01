package feature.live.server.data

import feature.live.server.data.storage.ReactionTotalStorage
import feature.live.server.domain.AddReactions
import feature.live.server.domain.GetReactionTotals
import platform.server.postgres.TransactionRunner
import platform.server.postgres.tables.ReactionTotalRow

internal class ReactionRepository(
    private val storage: ReactionTotalStorage,
    private val transactionRunner: TransactionRunner,
) {
    val getReactionTotals = GetReactionTotals {
        storage.list().toTotals()
    }

    val addReactions = AddReactions { counts ->
        transactionRunner.inTransaction {
            storage.upsertIncrements(
                counts.map { (reaction, count) -> ReactionTotalRow(reaction = reaction.name, total = count.toLong()) },
            )
            storage.list().toTotals()
        }
    }
}
