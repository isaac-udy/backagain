package feature.deck.client.domain

/** Makes this screen the stage, or stops it being one. The address keeps the choice across a reload. */
fun interface SetStage {
    suspend operator fun invoke(isStage: Boolean)
}
