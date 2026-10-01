package feature.live.client.domain

import kotlinx.coroutines.flow.StateFlow

fun interface FlowOfLiveState {
    operator fun invoke(): StateFlow<LiveState>
}
