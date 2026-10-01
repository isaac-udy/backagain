package feature.deck.client.data

import feature.deck.client.data.storage.AddressBarStorage
import feature.deck.client.domain.FlowOfIsStage
import feature.deck.client.domain.FlowOfSlidesFromHistory
import feature.deck.client.domain.GetStartAddress
import feature.deck.client.domain.SetStage
import feature.deck.client.domain.ShowSlideAddress
import feature.deck.client.domain.StartAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Slides live at `/s/{slideId}`. The server answers every path with the app, so any of these can
 * be opened directly. Each slide is an entry in the browser's history, so back and forward move
 * through the deck; steps within a slide are not.
 */
internal class AddressRepository(
    private val addressBar: AddressBarStorage,
) {
    private val slidePrefix = "/s/"
    private val presentPath = "/present"
    private val stageParameter = "stage"

    // Read once, before the deck starts rewriting the address to follow the current slide.
    private val startAddress = StartAddress(
        slideId = addressBar.path.slideId(),
        wantsToPresent = addressBar.path == presentPath,
    )

    private val isStage = MutableStateFlow(stageParameter in addressBar.queryParameters())

    val getStartAddress = GetStartAddress { startAddress }

    val flowOfIsStage = FlowOfIsStage { isStage }

    val setStage = SetStage { stage ->
        isStage.value = stage
        val others = addressBar.queryParameters() - stageParameter
        val parameters = if (stage) others + stageParameter else others
        addressBar.updateQuery(if (parameters.isEmpty()) "" else parameters.joinToString("&", prefix = "?"))
    }

    val showSlideAddress = ShowSlideAddress { slideId ->
        // The address the deck was opened at, like `/present`, isn't a slide to come back to.
        addressBar.showPath(slidePrefix + slideId, replace = addressBar.path.slideId() == null)
    }

    val flowOfSlidesFromHistory = FlowOfSlidesFromHistory {
        addressBar.historyMoves().mapNotNull { it.slideId() }
    }

    private fun AddressBarStorage.queryParameters(): List<String> =
        query.removePrefix("?").split("&").filter { it.isNotEmpty() }

    private fun String.slideId(): String? = removePrefix(slidePrefix).takeIf { startsWith(slidePrefix) && it.isNotBlank() }
}
