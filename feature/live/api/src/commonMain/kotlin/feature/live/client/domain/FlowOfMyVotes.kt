package feature.live.client.domain

import kotlinx.coroutines.flow.StateFlow

/** The option this browser last voted for in each poll, by poll id. */
fun interface FlowOfMyVotes {
    operator fun invoke(): StateFlow<Map<String, String>>
}
