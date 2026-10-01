package feature.deck.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import feature.deck.client.ui.content.ContentSlide
import platform.design.BackAgainColors
import platform.design.BackAgainPreviewFrame

@Composable
@NavigationDestination(ContentSlideDestination::class)
fun ContentSlideScreen(
    viewModel: ContentSlideViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    ContentSlideScreenContent(state = state)
}

@Composable
internal fun ContentSlideScreenContent(
    state: ContentSlideState,
) {
    SlideSteps(slideId = state.slideId) {
        ContentSlide(slideId = state.slideId, live = state.live)
    }
}

@Preview
@Composable
internal fun ContentSlideScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        SlideCanvas {
            ContentSlideScreenContent(state = ContentSlideState(slideId = "map-back"))
        }
    }
}
