package feature.live.server.data

import feature.live.server.data.storage.DeckStateStorage
import feature.live.server.domain.GetDeckState
import feature.live.server.domain.UpdateDeckState
import platform.server.postgres.TransactionRunner
import platform.server.postgres.tables.DeckStateRow
import kotlin.time.Clock

internal class DeckStateRepository(
    private val storage: DeckStateStorage,
    private val transactionRunner: TransactionRunner,
    private val clock: Clock,
) {
    private val firstSlideId = "cover"

    val getDeckState = GetDeckState {
        storage.getCurrent().toDomain()
    }

    val updateDeckState = UpdateDeckState { update ->
        transactionRunner.inTransaction {
            val current = storage.getCurrent()
            val next = DeckStateRow(
                id = 1,
                slideId = current?.slideId ?: firstSlideId,
                step = current?.step ?: 0,
                isLive = current?.isLive ?: false,
                updatedAt = clock.now(),
            ).let { row ->
                when (update) {
                    is UpdateDeckState.Update.MoveTo -> row.copy(slideId = update.position.slideId, step = update.position.step)
                    is UpdateDeckState.Update.SetLive -> row.copy(isLive = update.isLive)
                }
            }
            storage.upsert(next)
            next.toDomain()
        }
    }
}
