package feature.deck.client.ui

import dev.enro.NavigationKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NavigationKey.ContentSlideDestination")
data class ContentSlideDestination(val slideId: String) : NavigationKey
