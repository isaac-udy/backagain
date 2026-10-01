package feature.deck.client.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.isaacudy.udytils.state.AsyncState
import feature.deck.client.ui.content.WithJoinCode
import feature.live.Poll
import feature.live.PollTally
import feature.live.Polls
import platform.design.BackAgainColors
import platform.design.BackAgainPreviewFrame
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme

@Composable
@NavigationDestination(PollSlideDestination::class)
fun PollSlideScreen(
    viewModel: PollSlideViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    PollSlideScreenContent(
        state = state,
        onVote = viewModel::onVote,
    )
}

@Composable
internal fun PollSlideScreenContent(
    state: PollSlideState,
    onVote: (String) -> Unit,
) {
    SlideSteps(slideId = state.slideId) {
        val step = LocalSlideStep.current
        val showsResults = state.resultsFromStep?.let { step >= it } ?: false
        SlideFrame(title = state.poll.question) {
            val body: @Composable ColumnScope.() -> Unit = {
                PollOptions(
                    poll = state.poll,
                    tally = state.tally.takeIf { showsResults },
                    myVote = state.myVote,
                    onVote = onVote,
                )
                if (LocalSlideFormat.current == SlideFormat.Wide) {
                    Spacer(Modifier.weight(1f))
                }
                val total = state.tally.total
                val votes = "$total ${if (total == 1) "vote" else "votes"}"
                Text(
                    text = when (val voting = state.voting) {
                        is AsyncState.Error -> voting.error.message ?: "That vote didn't count"
                        else -> if (showsResults) "$votes · tap to vote" else "$votes in"
                    },
                    style = if (showsResults) BackAgainTheme.slideTypography.caption else BackAgainTheme.slideTypography.body,
                    color = if (state.voting is AsyncState.Error) BackAgainTheme.colors.error else BackAgainTheme.colors.onSurfaceVariant,
                )
            }
            if (state.showsJoinCode) WithJoinCode(body) else body()
        }
    }
}

/**
 * A poll's options, as bars of [tally] when there is one. Tapping one votes for it, unless the poll
 * doesn't accept it.
 */
@Composable
internal fun PollOptions(
    poll: Poll,
    tally: PollTally?,
    myVote: String?,
    modifier: Modifier = Modifier,
    onVote: (String) -> Unit = {},
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
        poll.options.forEach { option ->
            PollOptionBar(
                option = option,
                votes = tally?.let { it.votes[option.id] ?: 0 },
                total = tally?.total ?: 0,
                isMine = myVote == option.id,
                enabled = poll.accepts(option.id),
                onClick = { onVote(option.id) },
            )
        }
    }
}

/**
 * An option as a card with a progress bar, or just its label while [votes] are hidden. Your own vote
 * is outlined in the accent, with a tick; one that isn't [enabled] is greyed out and says so.
 */
@Composable
private fun PollOptionBar(
    option: Poll.Option,
    votes: Int?,
    total: Int,
    isMine: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = BackAgainTheme.colors
    val share by animateFloatAsState(if (total == 0 || votes == null) 0f else votes.toFloat() / total)
    val label = @Composable { modifier: Modifier ->
        Text(
            text = option.label,
            style = BackAgainTheme.slideTypography.body,
            color = colors.onSurface,
            textDecoration = if (enabled) null else TextDecoration.LineThrough,
            modifier = modifier,
        )
    }
    val tick = @Composable {
        if (isMine) {
            Icon(Icons.Filled.Check, contentDescription = "Your vote", tint = colors.live, modifier = Modifier.size(28.dp))
        }
    }
    val notAccepted = @Composable {
        if (!enabled) Pill("Not accepted")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clip(BackAgainShapes.medium)
            .background(colors.surface)
            .border(if (isMine) 2.dp else 1.dp, if (isMine) colors.accent else colors.outline, BackAgainShapes.medium)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = BackAgainSpacing.lg, vertical = BackAgainSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg),
    ) {
        // Without the bars, the label has the row to itself.
        val labelModifier = Modifier.weight(if (votes != null) 0.5f else 1f)
        if (LocalSlideFormat.current == SlideFormat.Tall) {
            // Too narrow for the label and its pill side by side.
            Column(
                modifier = labelModifier,
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    label(Modifier.weight(1f))
                    tick()
                }
                notAccepted()
            }
        } else {
            Row(
                modifier = labelModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            ) {
                label(Modifier.weight(1f))
                notAccepted()
                tick()
            }
        }
        if (votes != null) {
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(colors.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(share)
                        .clip(CircleShape)
                        .background(if (isMine) colors.live else colors.accent),
                )
            }
            Text(
                text = votes.toString(),
                style = BackAgainTheme.slideTypography.code,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.08f),
            )
        }
    }
}

@Preview
@Composable
internal fun PollSlideScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        SlideCanvas {
            PollSlideScreenContent(
                state = PollSlideState(
                    poll = Polls.UsesKotlin,
                    tally = PollTally(pollId = Polls.UsesKotlin.id, votes = mapOf("yes" to 12, "sometimes" to 7, "what" to 3)),
                    myVote = "yes",
                ),
                onVote = {},
            )
        }
    }
}
