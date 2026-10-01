package feature.live.client.domain

import kotlinx.coroutines.flow.StateFlow

/** Whether this browser holds a presenter session. */
fun interface FlowOfIsPresenter {
    operator fun invoke(): StateFlow<Boolean>
}
