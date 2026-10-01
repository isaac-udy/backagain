package feature.live.server.domain

import feature.live.LiveEvent

fun interface PublishLiveEvent {
    suspend operator fun invoke(event: LiveEvent)
}
