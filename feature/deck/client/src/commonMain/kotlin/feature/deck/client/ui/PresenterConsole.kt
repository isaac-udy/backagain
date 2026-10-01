package feature.deck.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import feature.deck.client.domain.Slide
import feature.deck.client.domain.Talk
import feature.live.Question
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme
import platform.design.components.BackAgainButton
import platform.design.components.BackAgainButtonVariant

/**
 * The presenter's notes: what's on screen, what to say about it, and two buttons big enough to hit
 * without looking. The slide itself is on the projector, one tap away.
 */
@Composable
internal fun PresenterConsole(
    state: DeckState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onHideQuestion: (String) -> Unit,
    onShowSlide: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = BackAgainTheme.colors
    val slide = state.slide
    val upNext = Talk.deck.slides.getOrNull(state.slideNumber)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = MaxWidth)
            .fillMaxWidth()
            .padding(BackAgainSpacing.md),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ModeChip(state = state, modifier = Modifier.weight(1f))
            NotesButton(showingNotes = true, onClick = onShowSlide)
            SettingsButton(onClick = onOpenSettings)
        }
        Text(
            text = "${state.slideNumber} / ${state.slideCount}" +
                if (slide.steps > 0) " · step ${state.position.step + 1} of ${slide.steps + 1}" else "",
            style = BackAgainTheme.typography.label,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = slide.title,
            style = BackAgainTheme.typography.display,
            color = colors.onSurface,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(BackAgainShapes.large)
                .background(colors.surface)
                .verticalScroll(rememberScrollState())
                .padding(BackAgainSpacing.md),
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            Text(
                text = slide.notes.unwrapped().ifBlank { "No notes for this slide." },
                style = BackAgainTheme.typography.body,
                color = colors.onSurface,
            )
            if (slide.kind == Slide.Kind.Questions) {
                OnScreenQuestions(questions = state.live.questions, onHide = onHideQuestion)
            }
        }
        if (upNext != null) {
            Text(
                text = "Up next: ${upNext.title}",
                style = BackAgainTheme.typography.label,
                color = colors.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            ConsoleButton(label = "Back", onClick = onPrevious, primary = false, modifier = Modifier.weight(1f))
            ConsoleButton(label = "Next", onClick = onNext, primary = true, modifier = Modifier.weight(2f))
        }
    }
}

private val MaxWidth = 720.dp

/** The questions on every screen, each with a way to take it off. */
@Composable
private fun OnScreenQuestions(
    questions: List<Question>,
    onHide: (String) -> Unit,
) {
    val colors = BackAgainTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        Text(text = "On screen (${questions.size})", style = BackAgainTheme.typography.label, color = colors.onSurfaceVariant)
        questions.forEach { question ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            ) {
                Text(
                    text = question.text,
                    style = BackAgainTheme.typography.body,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                BackAgainButton(
                    label = "Hide",
                    onClick = { onHide(question.id) },
                    variant = BackAgainButtonVariant.Ghost,
                )
            }
        }
    }
}

/** Notes are wrapped to fit the source file; only a blank line is a real paragraph break. */
private fun String.unwrapped(): String =
    split("\n\n").joinToString("\n\n") { paragraph -> paragraph.lines().joinToString(" ") { it.trim() } }

@Composable
private fun ConsoleButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = BackAgainTheme.colors
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(BackAgainShapes.large)
            .background(if (primary) colors.accent else colors.surface)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = BackAgainTheme.typography.display,
            color = if (primary) colors.onAccent else colors.onSurface,
        )
    }
}
