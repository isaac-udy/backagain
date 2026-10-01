package feature.live.client.data.storage

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal actual class PresenterTokenStorage actual constructor() {

    actual val token: StateFlow<String?>
        field = MutableStateFlow(localStorage.getItem(KEY))

    actual fun setToken(token: String?) {
        if (token == null) localStorage.removeItem(KEY) else localStorage.setItem(KEY, token)
        this.token.value = token
    }

    private companion object {
        const val KEY = "backagain.presenter-token"
    }
}
