package feature.deck.client.domain

fun interface GetStartAddress {
    suspend operator fun invoke(): StartAddress
}
