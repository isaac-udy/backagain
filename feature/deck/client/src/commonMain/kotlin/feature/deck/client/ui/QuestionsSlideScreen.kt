package feature.deck.client.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.isaacudy.udytils.state.AsyncState
import feature.deck.client.domain.Talk
import feature.deck.client.ui.content.QrCode
import feature.live.Question
import platform.design.BackAgainColors
import platform.design.BackAgainPreviewFrame
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme
import platform.design.components.BackAgainButton
import kotlin.time.Instant

@Composable
@NavigationDestination(QuestionsSlideDestination::class)
fun QuestionsSlideScreen(
    viewModel: QuestionsSlideViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    QuestionsSlideScreenContent(
        state = state,
        onDraftChanged = viewModel::onDraftChanged,
        onAsk = viewModel::onAsk,
    )
}

/**
 * The closing slide, left up through Q&A. The big screen shows the questions as they're asked,
 * beside the ways in; anywhere else gets a box to ask one, and a link to the code.
 */
@Composable
internal fun QuestionsSlideScreenContent(
    state: QuestionsSlideState,
    onDraftChanged: (String) -> Unit,
    onAsk: () -> Unit,
) {
    val typography = BackAgainTheme.slideTypography
    val colors = BackAgainTheme.colors
    val heading = @Composable {
        Text(highlighted("*Questions?*"), style = typography.hero, color = colors.onSurface)
    }
    val repository = @Composable {
        Text(
            text = buildAnnotatedString {
                withLink(LinkAnnotation.Url("https://${Talk.REPOSITORY}", TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)))) {
                    append(Talk.REPOSITORY)
                }
            },
            style = typography.caption,
            color = colors.accent,
            maxLines = 1,
            softWrap = false,
        )
    }
    if (LocalSlideFormat.current == SlideFormat.Tall) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(BackAgainSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            heading()
            Text("Ask out loud,\nor ask here.", style = typography.body, color = colors.onSurface)
            AskBox(state = state, onDraftChanged = onDraftChanged, onAsk = onAsk)
            OnScreenQuestions(questions = state.questions, shown = TALL_QUESTIONS_SHOWN)
            repository()
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 72.dp, vertical = BackAgainSpacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xxl),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
            ) {
                heading()
                if (LocalIsStage.current) {
                    Text("Ask out loud,\nor ask on your phone.", style = typography.body, color = colors.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.xl)) {
                        QrCode(address = Talk.ADDRESS, label = "Join the deck", size = 150.dp)
                        QrCode(address = Talk.REPOSITORY, label = "Get the code", size = 150.dp)
                    }
                } else {
                    Text("Ask out loud,\nor ask here.", style = typography.body, color = colors.onSurface)
                    AskBox(state = state, onDraftChanged = onDraftChanged, onAsk = onAsk)
                    repository()
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            ) {
                OnScreenQuestions(
                    questions = state.questions,
                    shown = WIDE_QUESTIONS_SHOWN,
                    modifier = Modifier.weight(1f).padding(top = BackAgainSpacing.xl),
                )
            }
        }
    }
}

@Composable
private fun AskBox(
    state: QuestionsSlideState,
    onDraftChanged: (String) -> Unit,
    onAsk: () -> Unit,
) {
    val colors = BackAgainTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        OutlinedTextField(
            value = state.draft,
            onValueChange = onDraftChanged,
            // Keys the field has finished with stop here, so the deck doesn't turn them into slide changes.
            modifier = Modifier.fillMaxWidth().onKeyEvent { true },
            textStyle = BackAgainTheme.slideTypography.caption,
            placeholder = { Text("Your question", style = BackAgainTheme.slideTypography.caption) },
            minLines = 2,
            shape = BackAgainShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.outline,
                cursorColor = colors.caret,
                focusedPlaceholderColor = colors.onSurfaceVariant,
                unfocusedPlaceholderColor = colors.onSurfaceVariant,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onAsk() }),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackAgainButton(
                label = if (state.asking is AsyncState.Loading) "Sending…" else "Send",
                onClick = onAsk,
                enabled = state.canAsk,
            )
            Text(
                text = when (val asking = state.asking) {
                    is AsyncState.Error -> asking.error.message ?: "That didn't send"
                    is AsyncState.Success -> "Sent. It's on the big screen."
                    else -> "${state.draft.trim().length} / ${Question.MAX_LENGTH}"
                },
                style = BackAgainTheme.slideTypography.caption,
                color = if (state.asking is AsyncState.Error) colors.error else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OnScreenQuestions(
    questions: List<Question>,
    shown: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        questions.take(shown).forEach { question -> QuestionBalloon(question) }
    }
}

/** A question, arriving the way the IDE's notifications do. */
@Composable
private fun QuestionBalloon(question: Question) {
    val colors = BackAgainTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, BackAgainShapes.medium)
            .background(colors.surface, BackAgainShapes.medium)
            .border(1.dp, colors.outline, BackAgainShapes.medium)
            .padding(BackAgainSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Help,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = question.text,
            style = BackAgainTheme.slideTypography.caption,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private const val WIDE_QUESTIONS_SHOWN = 5
private const val TALL_QUESTIONS_SHOWN = 10

@Preview
@Composable
internal fun QuestionsSlideScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        SlideCanvas {
            QuestionsSlideScreenContent(
                state = QuestionsSlideState(
                    questions = listOf(
                        Question(id = "2", text = "How big is the download on a phone?", askedAt = Instant.fromEpochSeconds(0)),
                        Question(id = "1", text = "Would you use this for a content site?", askedAt = Instant.fromEpochSeconds(0)),
                    ),
                    draft = "Does it work offline",
                ),
                onDraftChanged = {},
                onAsk = {},
            )
        }
    }
}
