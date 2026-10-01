package feature.live.client.domain

fun interface AskQuestion {
    suspend operator fun invoke(text: String)
}
