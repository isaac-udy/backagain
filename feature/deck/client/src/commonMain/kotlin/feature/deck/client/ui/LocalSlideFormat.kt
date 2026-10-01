package feature.deck.client.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Which layout [SlideCanvas] chose for the screen the slide is on. */
val LocalSlideFormat = staticCompositionLocalOf { SlideFormat.Wide }
