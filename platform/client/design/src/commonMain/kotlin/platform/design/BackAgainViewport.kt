package platform.design

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How much room the UI has, reduced to the one question layouts actually ask.
 *
 * Deliberately not a full breakpoint ladder: [isCompact] is a single boolean, so a layout either
 * has room or it doesn't. Multi-step breakpoints multiply the number of states every screen must be
 * designed and snapshotted in, and the middle steps are the ones nobody checks.
 */
@Immutable
data class BackAgainViewport(
    val widthDp: Dp,
    /** True when the UI is phone-width: prefer a single column and full-width controls. */
    val isCompact: Boolean,
) {
    companion object {
        /** Below this the UI is treated as compact. */
        val CompactMaxWidth: Dp = 600.dp

        fun of(widthDp: Dp): BackAgainViewport = BackAgainViewport(
            widthDp = widthDp,
            isCompact = widthDp < CompactMaxWidth,
        )

        /** The value in effect outside [ProvideBackAgainViewport]: a laptop-sized screen. */
        val Default: BackAgainViewport = of(1280.dp)
    }
}

internal val LocalBackAgainViewport = staticCompositionLocalOf { BackAgainViewport.Default }

/**
 * Measures the available width and publishes it as [BackAgainTheme.viewport] to [content].
 *
 * Wrap this once at the app root, inside [BackAgainTheme]. Nesting it is legal — an inner call
 * re-measures for a pane — but a screen that reaches for it usually wants a plain layout instead.
 */
@Composable
fun ProvideBackAgainViewport(
    content: @Composable () -> Unit,
) {
    BoxWithConstraints {
        CompositionLocalProvider(
            LocalBackAgainViewport provides BackAgainViewport.of(maxWidth),
            content = content,
        )
    }
}
