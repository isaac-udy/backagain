package feature.live.client.data.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal actual class PresenterTokenStorage actual constructor() {

    actual val token: StateFlow<String?>
        field = MutableStateFlow(null)

    actual fun setToken(token: String?) {
        this.token.value = token
    }
}
