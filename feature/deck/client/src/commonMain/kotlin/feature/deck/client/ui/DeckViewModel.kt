package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.navigationHandle
import dev.enro.open
import dev.isaacudy.udytils.coroutines.JobManager
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.viewModelState
import feature.deck.client.domain.DeckMode
import feature.deck.client.domain.FlowOfIsStage
import feature.deck.client.domain.FlowOfSlidesFromHistory
import feature.deck.client.domain.GetStartAddress
import feature.deck.client.domain.ShowSlideAddress
import feature.deck.client.domain.Talk
import feature.live.DeckPosition
import feature.live.Reaction
import feature.live.client.domain.FlowOfIsPresenter
import feature.live.client.domain.FlowOfLiveState
import feature.live.client.domain.FlowOfReactionBursts
import feature.live.client.domain.HideQuestion
import feature.live.client.domain.SendReaction
import feature.live.client.domain.UpdatePresentation
import kotlinx.coroutines.launch
import kotlin.random.Random

class DeckViewModel(
    private val flowOfLiveState: FlowOfLiveState,
    private val flowOfIsPresenter: FlowOfIsPresenter,
    private val flowOfReactionBursts: FlowOfReactionBursts,
    private val updatePresentation: UpdatePresentation,
    private val sendReaction: SendReaction,
    private val hideQuestion: HideQuestion,
    private val getStartAddress: GetStartAddress,
    private val flowOfIsStage: FlowOfIsStage,
    private val showSlideAddress: ShowSlideAddress,
    private val flowOfSlidesFromHistory: FlowOfSlidesFromHistory,
) : ViewModel() {

    private val navigation by navigationHandle<DeckDestination>()
    private val jobManager = JobManager(viewModelScope)
    private val deck = Talk.deck

    val state: ViewModelState<DeckState> = viewModelState(DeckState())

    init {
        viewModelScope.launch {
            val start = getStartAddress()
            state.update {
                copy(localPosition = deck.resolve(start.slideId?.let { DeckPosition(slideId = it, step = 0) }))
            }
        }
        viewModelScope.launch {
            flowOfIsStage().collect { isStage -> state.update { copy(isStage = isStage) } }
        }
        viewModelScope.launch {
            flowOfLiveState().collect { live -> state.update { copy(live = live) } }
        }
        viewModelScope.launch {
            flowOfIsPresenter().collect { isPresenter ->
                state.update {
                    // A presenter picks up where the talk is, rather than dragging everyone back
                    // to wherever this screen happened to be when they signed in.
                    val startsPresenting = isPresenter && !this.isPresenter
                    copy(
                        isPresenter = isPresenter,
                        localPosition = if (startsPresenting) deck.resolve(live.position ?: localPosition) else localPosition,
                    )
                }
            }
        }
        viewModelScope.launch {
            flowOfReactionBursts().collect(::showBurst)
        }
        viewModelScope.launch {
            flowOfSlidesFromHistory().collect(::moveToSlide)
        }
    }

    /** Opened at `/present`. Called once the deck is on screen, when there is a container to open into. */
    fun onDeckShown() {
        viewModelScope.launch {
            if (getStartAddress().wantsToPresent && !flowOfIsPresenter().value) {
                navigation.open(PresenterSignInDestination)
            }
        }
    }

    fun onNext() {
        deck.next(state.value.position)?.let(::moveTo)
    }

    fun onPrevious() {
        deck.previous(state.value.position)?.let(::moveTo)
    }

    fun onRejoin() {
        state.update { copy(isDetached = false) }
    }

    fun onOpenSettings() {
        navigation.open(DeckSettingsDestination)
    }

    fun onReact(reaction: Reaction) {
        viewModelScope.launch { runCatching { sendReaction(reaction) } }
    }

    fun onHideQuestion(id: String) {
        viewModelScope.launch { runCatching { hideQuestion(id) } }
    }

    fun onSlideShown(slideId: String) {
        viewModelScope.launch { showSlideAddress(slideId) }
    }

    fun onReactionFinished(id: Long) {
        state.update { copy(floatingReactions = floatingReactions.filterNot { it.id == id }) }
    }

    /** Going back lands on a slide as it was left, fully built; going forward, as it starts. */
    private fun moveToSlide(slideId: String) {
        val slide = deck.slide(slideId) ?: return
        val current = state.value.position
        if (slideId == current.slideId) return
        val isBack = deck.slides.indexOf(slide) < deck.indexOf(current)
        moveTo(DeckPosition(slideId = slideId, step = if (isBack) slide.steps else 0))
    }

    /**
     * A presenter's move is applied here straight away and sent to the server, which tells everyone
     * else. A follower who moves is no longer following: they've detached until they rejoin.
     */
    private fun moveTo(target: DeckPosition) {
        when (state.value.mode) {
            DeckMode.Presenting -> {
                state.update { copy(localPosition = target) }
                jobManager.launchReplacing(MOVE) {
                    runCatching { updatePresentation.moveTo(target) }
                }
            }
            DeckMode.Following, DeckMode.Detached -> state.update { copy(localPosition = target, isDetached = true) }
            DeckMode.Browsing -> state.update { copy(localPosition = target) }
        }
    }

    private fun showBurst(burst: Map<Reaction, Int>) {
        val reactions = burst
            .flatMap { (reaction, count) -> List(count) { reaction } }
            .shuffled()
            .take(MAX_PER_BURST)
        state.update {
            val firstId = (floatingReactions.maxOfOrNull { it.id } ?: 0L) + 1
            val added = reactions.mapIndexed { index, reaction ->
                DeckState.FloatingReaction(id = firstId + index, reaction = reaction, lane = Random.nextFloat())
            }
            copy(floatingReactions = (floatingReactions + added).takeLast(MAX_ON_SCREEN))
        }
    }

    private companion object {
        const val MOVE = "move"
        const val MAX_PER_BURST = 12
        const val MAX_ON_SCREEN = 60
    }
}
