package feature.deck.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import platform.design.BackAgainTheme
import platform.design.ProvideCompactSlideTypography

/**
 * Lays a slide out the same way on every screen.
 *
 * On a landscape screen the slide is a fixed 1280 × 720 dp canvas, scaled by changing the density
 * its content is measured in, and letterboxed. Nothing inside needs to know how big the real
 * screen is, so a slide on a laptop and the same slide on the projector are identical. On a
 * portrait phone a letterboxed 16:9 slide would be too small to read, so the slide gets a 440 dp
 * wide column instead, the compact type scale, and [SlideFormat.Tall] to reflow by.
 */
@Composable
internal fun SlideCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BackAgainTheme.colors.background),
    ) {
        val density = LocalDensity.current
        if (maxWidth >= maxHeight) {
            val scale = minOf(maxWidth / WideWidth, maxHeight / WideHeight)
            CompositionLocalProvider(
                LocalDensity provides Density(density.density * scale, density.fontScale),
                LocalSlideFormat provides SlideFormat.Wide,
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .requiredSize(WideWidth, WideHeight),
                ) {
                    content()
                }
            }
        } else {
            val scale = maxWidth / TallWidth
            CompositionLocalProvider(
                LocalDensity provides Density(density.density * scale, density.fontScale),
                LocalSlideFormat provides SlideFormat.Tall,
            ) {
                ProvideCompactSlideTypography {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()
                    }
                }
            }
        }
    }
}

private val WideWidth = 1280.dp
private val WideHeight = 720.dp
private val TallWidth = 440.dp
