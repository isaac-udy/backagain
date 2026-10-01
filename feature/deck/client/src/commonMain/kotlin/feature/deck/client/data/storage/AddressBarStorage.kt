package feature.deck.client.data.storage

import kotlinx.coroutines.flow.Flow

/** The browser's address bar, and its history. */
internal expect class AddressBarStorage() {
    val path: String
    val query: String

    /** Moves to [path], keeping the query: as a new history entry, or in place of this one if [replace]. */
    fun showPath(path: String, replace: Boolean)

    /** Replaces the query, `?` included, in place of this history entry. */
    fun updateQuery(query: String)

    /** The paths the browser's back and forward buttons return to. */
    fun historyMoves(): Flow<String>
}
