package feature.deck.client.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import platform.design.BackAgainTheme
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

/**
 * A sideways swipe anywhere over [content] moves the deck. The slide stays put while the screen
 * dims and an arrow grows to say which way it will go; letting go before the arrow lights up
 * cancels. [content] sees the touch first, so a field or a scrolling panel that claims the drag
 * keeps it.
 */
@Composable
internal fun SlideSwipe(
    enabled: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // Negative towards the next slide, positive towards the previous; ±1 is far enough to move.
    val progress = remember { Animatable(0f) }
    val next by rememberUpdatedState(onNext)
    val previous by rememberUpdatedState(onPrevious)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!enabled) Modifier else Modifier.pointerInput(Unit) {
                    val threshold = SwipeThreshold.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (down.type != PointerType.Touch) return@awaitEachGesture
                        var dragged = 0f
                        val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                            change.consume()
                            dragged = overSlop
                        } ?: return@awaitEachGesture
                        scope.launch { progress.snapTo(dragged / threshold) }
                        // Once the swipe has claimed the touch it keeps it until the finger lifts:
                        // Compose's horizontalDrag gives up on any move another handler consumed.
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == drag.id } ?: break
                            if (!change.pressed) break
                            dragged += change.positionChangeIgnoreConsumed().x
                            change.consume()
                            scope.launch { progress.snapTo((dragged / threshold).coerceIn(-MaxProgress, MaxProgress)) }
                        }
                        val move = when {
                            abs(dragged) < threshold -> null
                            dragged < 0 -> next
                            else -> previous
                        }
                        move?.invoke()
                        scope.launch { progress.animateTo(0f, tween(if (move != null) 250 else 150)) }
                    }
                },
            ),
    ) {
        content()
        SwipeIndicator(progress = progress.value)
    }
}

/**
 * The arrow starts out towards the edge it points at and travels with the finger, keeping pace at
 * first and easing off as it nears the middle of the screen.
 */
@Composable
private fun SwipeIndicator(progress: Float) {
    if (progress == 0f) return
    val colors = BackAgainTheme.colors
    val reach = abs(progress).coerceAtMost(1f)
    val armed = abs(progress) >= 1f
    val travel = 1f - exp(-ArrowEasing * abs(progress))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.75f * reach)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = sign(progress) * (travel * ArrowTravel.toPx() - ArrowStart.toPx())
                    alpha = reach
                    scaleX = 0.6f + 0.4f * reach
                    scaleY = 0.6f + 0.4f * reach
                }
                .size(88.dp)
                .clip(CircleShape)
                .background(if (armed) colors.accent else colors.surface)
                .padding(20.dp),
        ) {
            Icon(
                imageVector = if (progress < 0) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = if (armed) colors.onAccent else colors.onSurface,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val SwipeThreshold = 96.dp
private const val MaxProgress = 4f

private val ArrowStart = 100.dp
private val ArrowTravel = 110.dp

/** With [ArrowTravel], starts the arrow off at about 0.8 of the finger's speed. */
private const val ArrowEasing = 0.7f
