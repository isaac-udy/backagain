package feature.deck.client.data.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal actual class AddressBarStorage actual constructor() {
    private var currentPath = "/"
    private var currentQuery = ""

    actual val path: String get() = currentPath

    actual val query: String get() = currentQuery

    actual fun showPath(path: String, replace: Boolean) {
        currentPath = path
    }

    actual fun updateQuery(query: String) {
        currentQuery = query
    }

    actual fun historyMoves(): Flow<String> = emptyFlow()
}
