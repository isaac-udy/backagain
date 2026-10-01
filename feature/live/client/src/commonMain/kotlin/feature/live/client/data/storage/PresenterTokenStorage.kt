package feature.live.client.data.storage

import kotlinx.coroutines.flow.StateFlow

/** The presenter session token, kept across reloads so the presenter's phone stays signed in. */
internal expect class PresenterTokenStorage() {
    val token: StateFlow<String?>

    fun setToken(token: String?)
}
