package feature.live.server.data.storage

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import platform.server.postgres.tables.PresenterSessionRow
import platform.server.postgres.tables.PresenterSessionsTable
import platform.server.postgres.tables.setFromRow

internal class PresenterSessionStorage(
    private val database: Database,
) {
    suspend fun insert(row: PresenterSessionRow) {
        suspendTransaction(db = database) {
            PresenterSessionsTable.insert { it.setFromRow(row) }
        }
    }

    suspend fun get(tokenHash: String): PresenterSessionRow? = suspendTransaction(db = database) {
        PresenterSessionsTable
            .selectAll()
            .where { PresenterSessionsTable.tokenHash eq tokenHash }
            .map(::PresenterSessionRow)
            .singleOrNull()
    }

    suspend fun delete(tokenHash: String) {
        suspendTransaction(db = database) {
            PresenterSessionsTable.deleteWhere { PresenterSessionsTable.tokenHash eq tokenHash }
        }
    }
}
