package feature.live.server.data

import feature.live.server.data.storage.QuestionStorage
import feature.live.server.domain.AddQuestion
import feature.live.server.domain.GetQuestionsOnScreen
import feature.live.server.domain.MarkQuestionHidden
import platform.server.postgres.tables.QuestionRow
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class QuestionRepository(
    private val storage: QuestionStorage,
    private val clock: Clock,
) {
    val addQuestion = AddQuestion { clientId, text ->
        val row = QuestionRow(
            id = Uuid.random(),
            clientId = clientId,
            text = text,
            askedAt = clock.now(),
            hiddenAt = null,
        )
        storage.insert(row)
        row.toDomain()
    }

    val getQuestionsOnScreen = GetQuestionsOnScreen { limit ->
        storage.listOnScreen(limit).map { it.toDomain() }
    }

    val markQuestionHidden = MarkQuestionHidden { id ->
        val uuid = Uuid.parseOrNull(id) ?: return@MarkQuestionHidden false
        storage.updateHiddenAt(uuid, clock.now())
    }
}
