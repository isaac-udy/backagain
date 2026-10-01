package feature.deck.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

@NavigationDestination(PresenterSignInDestination::class)
val presenterSignInDestination: NavigationDestinationProvider<PresenterSignInDestination> =
    navigationDestination(metadata = { directOverlayWithFade() }) {
        val viewModel: PresenterSignInViewModel = viewModel()
        val state by viewModel.state.collectAsState()
        PresenterSignInScreenContent(
            state = state,
            onPasswordChanged = viewModel::onPasswordChanged,
            onSignIn = viewModel::onSignIn,
            onDismiss = viewModel::onDismiss,
        )
    }

@Composable
internal fun PresenterSignInScreenContent(
    state: PresenterSignInState,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Present", style = BackAgainTheme.typography.title)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(BackAgainSpacing.sm)) {
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    label = { Text("Presenter password") },
                    singleLine = true,
                    isError = state.signingIn is AsyncState.Error,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onSignIn() }),
                )
                val signingIn = state.signingIn
                if (signingIn is AsyncState.Error) {
                    Text(
                        text = signingIn.error.message ?: "That didn't work",
                        style = BackAgainTheme.typography.caption,
                        color = BackAgainTheme.colors.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSignIn, enabled = state.signingIn !is AsyncState.Loading) {
                Text(text = if (state.signingIn is AsyncState.Loading) "Signing in…" else "Sign in", color = BackAgainTheme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Preview
@Composable
internal fun PresenterSignInScreenPreview() {
    BackAgainPreviewFrame(colors = BackAgainColors.Dark) {
        PresenterSignInScreenContent(
            state = PresenterSignInState(password = "hunter2"),
            onPasswordChanged = {},
            onSignIn = {},
            onDismiss = {},
        )
    }
}
