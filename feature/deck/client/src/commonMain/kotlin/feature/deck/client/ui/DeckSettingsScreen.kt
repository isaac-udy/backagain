package feature.deck.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlayWithFade
import dev.isaacudy.udytils.state.AsyncState
import platform.design.BackAgainColors
import platform.design.BackAgainPreviewFrame
import platform.design.BackAgainSpacing
import platform.design.BackAgainTheme
import platform.design.components.BackAgainButton
import platform.design.components.BackAgainButtonVariant

@NavigationDestination(DeckSettingsDestination::class)
val deckSettingsDestination: NavigationDestinationProvider<DeckSettingsDestination> =
    navigationDestination(metadata = { directOverlayWithFade() }) {
        val viewModel: DeckSettingsViewModel = viewModel()
        val state by viewModel.state.collectAsState()
        DeckSettingsScreenContent(
            state = state,
            onToggleStage = viewModel::onToggleStage,
            onToggleLive = viewModel::onToggleLive,
            onResetAudience = viewModel::onResetAudience,
            onStartPresenting = viewModel::onStartPresenting,
            onStopPresenting = viewModel::onStopPresenting,
            onDismiss = viewModel::onDismiss,
        )
    }

/** This device's settings: its palette, and whether and how it presents. */
@Composable
internal fun DeckSettingsScreenContent(
    state: DeckSettingsState,
    onToggleStage: () -> Unit,
    onToggleLive: () -> Unit,
    onResetAudience: () -> Unit,
    onStartPresenting: () -> Unit,
    onStopPresenting: () -> Unit,
    onDismiss: () -> Unit,
) {
    val mode = BackAgainTheme.mode
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Settings", style = BackAgainTheme.typography.title)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.lg)) {
                SettingRow(
                    label = "Theme",
                    detail = "Just for this device.",
                ) {
                    BackAgainButton(
                        label = if (mode.isDark) "Dark" else "Light",
                        onClick = mode.toggle,
                        variant = BackAgainButtonVariant.Secondary,
                    )
                }
                SettingRow(
                    label = "Stage",
                    detail = if (state.isStage) {
                        "Just the slide, for the projector. The address in the corner opens these settings."
                    } else {
                        "For the projector: just the slide, with no controls."
                    },
                ) {
                    BackAgainButton(
                        label = if (state.isStage) "Turn off" else "Turn on",
                        onClick = onToggleStage,
                        variant = BackAgainButtonVariant.Secondary,
                    )
                }
                if (state.isPresenter) {
                    SettingRow(
                        label = if (state.isLive) "Live" else "Not live",
                        detail = if (state.isLive) "Everyone here follows along." else "Everyone browses at their own pace.",
                    ) {
                        BackAgainButton(
                            label = if (state.isLive) "End live" else "Go live",
                            onClick = onToggleLive,
                            variant = BackAgainButtonVariant.Secondary,
                        )
                    }
                    SettingRow(
                        label = "Reset the audience",
                        detail = when {
                            state.resetting is AsyncState.Success -> "Done: every vote, question and reaction is gone."
                            state.resetting is AsyncState.Error -> "That didn't work. Try again?"
                            state.confirmingReset -> "Clears every vote, question and reaction, for everyone. Sure?"
                            else -> "Before the next talk: clears every vote, question and reaction."
                        },
                    ) {
                        BackAgainButton(
                            label = when {
                                state.resetting is AsyncState.Loading -> "Resetting…"
                                state.confirmingReset -> "Yes, reset"
                                else -> "Reset…"
                            },
                            onClick = onResetAudience,
                            enabled = state.resetting !is AsyncState.Loading,
                            variant = if (state.confirmingReset) BackAgainButtonVariant.Primary else BackAgainButtonVariant.Secondary,
                        )
                    }
                    SettingRow(
                        label = "Presenting",
                        detail = "Stop, and this device follows like everyone else. Other presenting devices carry on.",
                    ) {
                        BackAgainButton(
                            label = "Stop",
                            onClick = onStopPresenting,
                            variant = BackAgainButtonVariant.Secondary,
                        )
                    }
                } else {
                    SettingRow(
                        label = "Presenting",
                        detail = "Drive the talk from this device.",
                    ) {
                        BackAgainButton(
                            label = "Present…",
                            onClick = onStartPresenting,
                            variant = BackAgainButtonVariant.Secondary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done", color = BackAgainTheme.colors.accent)
            }
        },
    )
}

@Composable
private fun SettingRow(
    label: String,
    detail: String,
    control: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BackAgainSpacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = BackAgainTheme.typography.body, color = BackAgainTheme.colors.onSurface)
            Text(text = detail, style = BackAgainTheme.typography.caption, color = BackAgainTheme.colors.onSurfaceVariant)
        }
        control()
    }
}

@Preview
@Composable
internal fun DeckSettingsScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        DeckSettingsScreenContent(
            state = DeckSettingsState(isPresenter = true, isLive = true),
            onToggleStage = {},
            onToggleLive = {},
            onResetAudience = {},
            onStartPresenting = {},
            onStopPresenting = {},
            onDismiss = {},
        )
    }
}
