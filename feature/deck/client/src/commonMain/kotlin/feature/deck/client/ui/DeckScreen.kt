package feature.deck.client.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import feature.deck.client.domain.DeckMode
import feature.deck.client.domain.Slide
import feature.live.Reaction
import feature.live.client.domain.LiveConnection
import feature.live.client.domain.LiveState
import platform.design.BackAgainColors
import platform.design.BackAgainPreviewFrame
import platform.design.BackAgainTheme

@Composable
@NavigationDestination(DeckDestination::class)
fun DeckScreen(
    viewModel: DeckViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.onDeckShown()
    }
    LaunchedEffect(state.slide.id) {
        viewModel.onSlideShown(state.slide.id)
    }
    DeckScreenContent(
        state = state,
        slides = { SlideDisplay(slide = state.slide, index = state.slideNumber) },
        onNext = viewModel::onNext,
        onPrevious = viewModel::onPrevious,
        onRejoin = viewModel::onRejoin,
        onReact = viewModel::onReact,
        onHideQuestion = viewModel::onHideQuestion,
        onOpenSettings = viewModel::onOpenSettings,
        onReactionFinished = viewModel::onReactionFinished,
    )
}

/**
 * Three layouts over one deck:
 * * the stage — the projector: the slide and the room's reactions, nothing to click;
 * * the presenter's notes — big buttons and what to say, shown on a phone until the presenter
 *   switches to the slide, and on a bigger screen when they switch to the notes;
 * * everyone else — the slide, with a bar to react and to wander off or rejoin.
 */
@Composable
internal fun DeckScreenContent(
    state: DeckState,
    slides: @Composable () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRejoin: () -> Unit,
    onReact: (Reaction) -> Unit,
    onHideQuestion: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onReactionFinished: (Long) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val isCompact = BackAgainTheme.viewport.isCompact
    var showNotes by remember(isCompact) { mutableStateOf(isCompact) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackAgainTheme.colors.background)
            .focusRequester(focusRequester)
            .focusable()
            // onKeyEvent rather than onPreviewKeyEvent: a focused text field gets the arrow keys
            // first, so typing on the wall slide never changes slide.
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionRight, Key.PageDown, Key.Spacebar -> onNext().let { true }
                    Key.DirectionLeft, Key.PageUp -> onPrevious().let { true }
                    else -> false
                }
            },
    ) {
        if (state.isStage) {
            SlideStage(state = state, slides = slides, onReactionFinished = onReactionFinished)
            StageBadge(
                viewers = state.live.viewers,
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
            return@Box
        }
        SlideSwipe(enabled = isCompact, onNext = onNext, onPrevious = onPrevious) {
            if (state.mode == DeckMode.Presenting && showNotes) {
                PresenterConsole(
                    state = state,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onHideQuestion = onHideQuestion,
                    onShowSlide = { showNotes = false },
                    onOpenSettings = onOpenSettings,
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SlideStage(
                        state = state,
                        slides = slides,
                        onReactionFinished = onReactionFinished,
                        modifier = Modifier.weight(1f),
                    )
                    DeckBar(
                        state = state,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onRejoin = onRejoin,
                        onReact = onReact,
                        onShowNotes = { showNotes = true },
                        onOpenSettings = onOpenSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun SlideStage(
    state: DeckState,
    slides: @Composable () -> Unit,
    onReactionFinished: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        SlideCanvas {
            CompositionLocalProvider(
                LocalDeckPosition provides state.position,
                LocalIsStage provides state.isStage,
            ) {
                slides()
            }
        }
        ReactionFloats(
            reactions = state.floatingReactions,
            onFinished = onReactionFinished,
        )
    }
}

/**
 * The slides are destinations in a container of their own, so each interactive slide gets its own
 * ViewModel and the deck gets Enro's transitions. The container always holds exactly one slide,
 * swapped for the next as the position moves; the direction of the swap picks the animation, and a
 * slide that grows from its neighbour fades in place.
 */
@Composable
private fun SlideDisplay(
    slide: Slide,
    index: Int,
) {
    val key = slide.destination()
    // acceptNone: only the swap below changes this container, so anything else opened from a slide
    // (the presenter sign-in) goes to the root container instead.
    val container = rememberNavigationContainer(
        backstack = backstackOf(key.asInstance()),
        filter = acceptNone(),
    )
    val forward = remember { mutableStateOf(true) }
    val grows = remember { mutableStateOf(false) }
    val shown = remember { mutableStateOf(slide to index) }
    LaunchedEffect(key) {
        if (container.backstack.lastOrNull()?.key != key) {
            val (shownSlide, shownIndex) = shown.value
            forward.value = index >= shownIndex
            grows.value = when (index - shownIndex) {
                1 -> slide.growsFromPrevious
                -1 -> shownSlide.growsFromPrevious
                else -> false
            }
            container.updateBackstack { backstackOf(key.asInstance()) }
        }
        shown.value = slide to index
    }
    val animations = remember {
        val transition: () -> ContentTransform = {
            val direction = if (forward.value) 1 else -1
            if (grows.value) {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(durationMillis = 300, delayMillis = 200)),
                    initialContentExit = fadeOut(tween(200)),
                )
            } else {
                ContentTransform(
                    targetContentEnter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { direction * it / 8 },
                    initialContentExit = fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { -direction * it / 8 },
                )
            }
        }
        NavigationAnimations(
            transitionSpec = { transition() },
            popTransitionSpec = { transition() },
        )
    }
    NavigationDisplay(
        state = container,
        modifier = Modifier.fillMaxSize(),
        animations = animations,
    )
}

private fun Slide.destination(): NavigationKey = when (val kind = kind) {
    Slide.Kind.Content -> ContentSlideDestination(slideId = id)
    Slide.Kind.Questions -> QuestionsSlideDestination
    is Slide.Kind.Poll -> PollSlideDestination(
        slideId = id,
        pollId = kind.pollId,
        showsJoinCode = kind.showsJoinCode,
        resultsFromStep = kind.resultsFromStep,
    )
}

@Preview
@Composable
internal fun DeckScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        DeckScreenContent(
            state = DeckState(
                live = LiveState.Initial.copy(connection = LiveConnection.Connected, isLive = true, viewers = 64),
            ),
            slides = { ContentSlideScreenContent(state = ContentSlideState(slideId = "cover")) },
            onNext = {},
            onPrevious = {},
            onRejoin = {},
            onReact = {},
            onHideQuestion = {},
            onOpenSettings = {},
            onReactionFinished = {},
        )
    }
}
