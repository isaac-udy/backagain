package feature.deck.client.ui

import dev.enro.NavigationKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NavigationKey.PollSlideDestination")
data class PollSlideDestination(
    val slideId: String,
    val pollId: String,
    val showsJoinCode: Boolean = false,
    val resultsFromStep: Int? = 0,
) : NavigationKey
