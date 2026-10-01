package feature.deck.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import feature.live.DeckPosition

/**
 * How many of the current slide's build steps have been revealed. Steps are not part of a slide's
 * navigation key, so revealing one never recreates the slide or changes the address.
 */
val LocalSlideStep = compositionLocalOf { 0 }

/** Where the deck is. A slide reads its own steps through [SlideSteps], not from this. */
val LocalDeckPosition = compositionLocalOf<DeckPosition?> { null }

/** Whether the slide is on the projector, where nobody can click or type. */
val LocalIsStage = compositionLocalOf { false }

/**
 * Provides [LocalSlideStep] for the slide [slideId]: the deck's step while the deck is on it, and
 * the step it was left at while it animates away, so a leaving slide doesn't un-build itself.
 */
@Composable
internal fun SlideSteps(
    slideId: String,
    content: @Composable () -> Unit,
) {
    val position = LocalDeckPosition.current
    val left = remember(slideId) { LeftAt() }
    if (position?.slideId == slideId) left.step = position.step
    CompositionLocalProvider(LocalSlideStep provides left.step, content = content)
}

private class LeftAt(var step: Int = 0)
