package feature.deck.client.domain

import kotlinx.coroutines.flow.Flow

/** The slides the browser's back and forward buttons go to. */
fun interface FlowOfSlidesFromHistory {
    operator fun invoke(): Flow<String>
}
