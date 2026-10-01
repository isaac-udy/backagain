package platform.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The frame a screen `@Preview` renders inside: a fixed-size screen with the palette pinned, so a
 * preview shows the app as it looks on that screen rather than stretched over the IDE's canvas.
 *
 * The default is the screen the deck is designed for: a 16:9 projector. Pass a phone's size to see
 * the portrait layout.
 */
@Composable
fun BackAgainPreviewFrame(
    colors: BackAgainColors,
    width: Dp = 1280.dp,
    height: Dp = 720.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.width(width).height(height)) {
        BackAgainTheme(colors = colors) {
            ProvideBackAgainViewport {
                content()
            }
        }
    }
}
