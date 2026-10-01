package feature.deck.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.navigationHandle
import dev.enro.requestClose
import dev.isaacudy.udytils.coroutines.JobManager
import dev.isaacudy.udytils.state.AsyncState
import dev.isaacudy.udytils.state.ViewModelState
import dev.isaacudy.udytils.state.fromSuspending
import dev.isaacudy.udytils.state.viewModelState
import feature.live.client.domain.SignInPresenter

class PresenterSignInViewModel(
    private val signInPresenter: SignInPresenter,
) : ViewModel() {

    private val navigation by navigationHandle<PresenterSignInDestination>()
    private val jobManager = JobManager(viewModelScope)

    val state: ViewModelState<PresenterSignInState> = viewModelState(PresenterSignInState())

    fun onPasswordChanged(password: String) {
        state.update { copy(password = password, signingIn = AsyncState.Idle()) }
    }

    fun onSignIn() {
        val password = state.value.password
        if (password.isEmpty()) return
        jobManager.launchReplacing(SIGN_IN) {
            AsyncState.fromSuspending { signInPresenter(password) }.collect { signingIn ->
                state.update { copy(signingIn = signingIn) }
                if (signingIn is AsyncState.Success) navigation.requestClose()
            }
        }
    }

    fun onDismiss() {
        navigation.requestClose()
    }

    private companion object {
        const val SIGN_IN = "signIn"
    }
}
