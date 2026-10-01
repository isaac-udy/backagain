package feature.deck.client.data.storage

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal actual class AddressBarStorage actual constructor() {
    actual val path: String get() = window.location.pathname

    actual val query: String get() = window.location.search

    actual fun showPath(path: String, replace: Boolean) {
        if (window.location.pathname == path) return
        val url = path + window.location.search
        if (replace) {
            window.history.replaceState(null, "", url)
        } else {
            window.history.pushState(null, "", url)
        }
    }

    actual fun updateQuery(query: String) {
        window.history.replaceState(null, "", window.location.pathname + query)
    }

    actual fun historyMoves(): Flow<String> = callbackFlow {
        window.onpopstate = { trySend(window.location.pathname) }
        awaitClose { window.onpopstate = null }
    }
}
