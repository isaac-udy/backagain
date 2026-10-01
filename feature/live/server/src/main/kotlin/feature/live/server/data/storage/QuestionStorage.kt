package feature.live.server.data.storage

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import platform.server.postgres.tables.QuestionRow
import platform.server.postgres.tables.QuestionsTable
import platform.server.postgres.tables.setFromRow
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A question is on screen from the moment it's asked until it's hidden. */
internal class QuestionStorage(
    private val database: Database,
) {
    suspend fun insert(row: QuestionRow) {
        suspendTransaction(db = database) {
            QuestionsTable.insert { it.setFromRow(row) }
        }
    }

    /** The questions on screen, newest first. */
    suspend fun listOnScreen(limit: Int): List<QuestionRow> = suspendTransaction(db = database) {
        QuestionsTable
            .selectAll()
            .where { QuestionsTable.hiddenAt.isNull() }
            .orderBy(QuestionsTable.askedAt, SortOrder.DESC)
            .limit(limit)
            .map(::QuestionRow)
    }

    /** @return false when no question on screen has [id]. */
    suspend fun updateHiddenAt(id: Uuid, hiddenAt: Instant): Boolean = suspendTransaction(db = database) {
        val updated = QuestionsTable.update(
            where = { (QuestionsTable.id eq id) and QuestionsTable.hiddenAt.isNull() },
        ) { it[QuestionsTable.hiddenAt] = hiddenAt }
        updated > 0
    }

    suspend fun deleteAll() {
        suspendTransaction(db = database) {
            QuestionsTable.deleteAll()
        }
    }
}
