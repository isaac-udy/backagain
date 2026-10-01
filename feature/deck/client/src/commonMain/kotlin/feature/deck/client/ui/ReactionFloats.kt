package feature.deck.client.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** Reactions rising up over the slide, one glyph per tap the server batched. */
@Composable
internal fun ReactionFloats(
    reactions: List<DeckState.FloatingReaction>,
    onFinished: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        reactions.forEach { floating ->
            key(floating.id) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    progress.animateTo(1f, tween(durationMillis = 2_800, easing = LinearOutSlowInEasing))
                    onFinished(floating.id)
                }
                Box(
                    modifier = Modifier.graphicsLayer {
                        val value = progress.value
                        val glyph = GlyphSize.toPx()
                        translationX = floating.lane * (width.toPx() - glyph) + sin(value * 9f + floating.lane * 6f) * 12.dp.toPx()
                        translationY = height.toPx() - glyph - value * height.toPx() * 0.75f
                        alpha = 1f - value * value * value
                        scaleX = 0.6f + value * 0.6f
                        scaleY = scaleX
                    },
                ) {
                    ReactionGlyph(reaction = floating.reaction, modifier = Modifier.size(GlyphSize))
                }
            }
        }
    }
}

private val GlyphSize = 40.dp
