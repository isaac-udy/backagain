package feature.live.server.data.storage

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import platform.server.postgres.tables.DeckStateRow
import platform.server.postgres.tables.DeckStateTable
import platform.server.postgres.tables.setFromRow

internal class DeckStateStorage(
    private val database: Database,
) {
    suspend fun getCurrent(): DeckStateRow? = suspendTransaction(db = database) {
        DeckStateTable.selectAll().map(::DeckStateRow).singleOrNull()
    }

    suspend fun upsert(row: DeckStateRow) {
        suspendTransaction(db = database) {
            DeckStateTable.upsert { it.setFromRow(row) }
        }
    }
}
