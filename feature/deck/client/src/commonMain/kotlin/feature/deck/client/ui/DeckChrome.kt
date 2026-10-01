package feature.deck.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import feature.deck.client.domain.DeckMode
import feature.deck.client.domain.Talk
import feature.live.Reaction
import feature.live.client.domain.LiveConnection
import platform.design.BackAgainShapes
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme
import platform.design.components.BackAgainButton

/** The deck's bar, drawn as the IDE's status bar. */
@Composable
internal fun DeckBar(
    state: DeckState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRejoin: () -> Unit,
    onReact: (Reaction) -> Unit,
    onShowNotes: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val isPresenting = state.mode == DeckMode.Presenting
    val showReactions = state.live.isLive && !isPresenting
    val outline = BackAgainTheme.colors.outline
    val barModifier = Modifier
        .fillMaxWidth()
        .background(BackAgainTheme.colors.surface)
        .drawBehind { drawLine(outline, Offset.Zero, Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
        .padding(horizontal = BackAgainSpacing.md, vertical = BackAgainSpacing.xs)

    if (BackAgainTheme.viewport.isCompact) {
        Column(
            modifier = barModifier,
            verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.xs),
        ) {
            Row(
                modifier = Modifier.padding(top = BackAgainSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeChip(state = state, modifier = Modifier.weight(1f))
                if (state.mode == DeckMode.Detached) BackAgainButton(label = "Back to live", onClick = onRejoin)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPresenting) NotesButton(showingNotes = false, onClick = onShowNotes)
                Spacer(Modifier.weight(1f))
                SlideNavigation(state = state, onNext = onNext, onPrevious = onPrevious)
                SettingsButton(onClick = onOpenSettings)
            }
            if (showReactions) {
                ReactionBar(onReact = onReact, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    } else {
        Row(
            modifier = barModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
            ) {
                ModeChip(state = state)
                if (state.mode == DeckMode.Detached) BackAgainButton(label = "Back to live", onClick = onRejoin)
            }
            if (showReactions) ReactionBar(onReact = onReact)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPresenting) NotesButton(showingNotes = false, onClick = onShowNotes)
                SlideNavigation(state = state, onNext = onNext, onPrevious = onPrevious)
                SettingsButton(onClick = onOpenSettings)
            }
        }
    }
}

/**
 * Where this screen stands relative to the talk. A live talk shows as the IDE's run widget: a
 * green ▶.
 */
@Composable
internal fun ModeChip(
    state: DeckState,
    modifier: Modifier = Modifier,
) {
    val colors = BackAgainTheme.colors
    val connected = state.live.connection == LiveConnection.Connected
    val (icon, tint, label) = when {
        !connected -> Triple(Icons.Filled.Sync, colors.onSurfaceVariant, "Reconnecting…")
        state.mode == DeckMode.Presenting -> Triple(
            Icons.Filled.PlayArrow,
            if (state.live.isLive) colors.live else colors.onSurfaceVariant,
            if (state.live.isLive) "Presenting, live" else "Presenting, not live",
        )
        state.mode == DeckMode.Following -> Triple(Icons.Filled.PlayArrow, colors.live, "Live")
        state.mode == DeckMode.Detached -> Triple(Icons.Filled.Pause, colors.onSurfaceVariant, "Exploring")
        else -> Triple(null, colors.onSurfaceVariant, "Browse at your own pace")
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm),
    ) {
        if (icon != null) StatusIcon(icon = icon, tint = tint)
        Text(
            text = if (connected && state.live.isLive) "$label · ${state.live.viewers} here" else label,
            style = BackAgainTheme.typography.label,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun StatusIcon(
    icon: ImageVector,
    tint: Color,
) {
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
}

@Composable
internal fun SettingsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = BackAgainTheme.colors.onSurfaceVariant)
    }
}

/** Switches a presenter between the notes and the slide; lit while the notes are showing. */
@Composable
internal fun NotesButton(
    showingNotes: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Notes,
            contentDescription = if (showingNotes) "Show the slide" else "Show the notes",
            tint = if (showingNotes) BackAgainTheme.colors.accent else BackAgainTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SlideNavigation(
    state: DeckState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous", tint = BackAgainTheme.colors.onSurface)
        }
        Text(
            text = "${state.slideNumber} / ${state.slideCount}",
            style = BackAgainTheme.typography.label.copy(fontFamily = BackAgainTheme.fonts.mono),
            color = BackAgainTheme.colors.onSurfaceVariant,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = BackAgainTheme.colors.onSurface)
        }
    }
}

@Composable
internal fun ReactionBar(
    onReact: (Reaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Reaction.entries.forEach { reaction ->
            IconButton(onClick = { onReact(reaction) }) {
                ReactionGlyph(reaction = reaction, modifier = Modifier.size(26.dp))
            }
        }
    }
}

/**
 * The projector's only chrome: where to join, and how many have. Clicking it opens the settings,
 * the way back from being the stage.
 */
@Composable
internal fun StageBadge(
    viewers: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${Talk.ADDRESS} · $viewers here",
        style = BackAgainTheme.typography.label,
        color = BackAgainTheme.colors.onSurfaceVariant,
        modifier = modifier
            .padding(BackAgainSpacing.md)
            .clip(BackAgainShapes.small)
            .background(BackAgainTheme.colors.surface)
            .clickable(onClickLabel = "Settings", onClick = onClick)
            .padding(horizontal = BackAgainSpacing.md, vertical = BackAgainSpacing.sm),
    )
}
