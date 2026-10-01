package feature.live

import kotlinx.serialization.Serializable

@Serializable
data class Poll(
    val id: String,
    val question: String,
    val options: List<Option>,
) {
    @Serializable
    data class Option(
        val id: String,
        val label: String,
        val enabled: Boolean = true,
    )

    /** Whether a vote for [optionId] counts. The phone asks before offering it; the server asks again. */
    fun accepts(optionId: String): Boolean =
        options.any { it.id == optionId && it.enabled }
}
