package feature.live.client.data

import feature.live.client.data.storage.PresenterTokenStorage
import feature.live.client.domain.FlowOfIsPresenter
import feature.live.client.domain.HideQuestion
import feature.live.client.domain.ResetAudience
import feature.live.client.domain.SignInPresenter
import feature.live.client.domain.SignOutPresenter
import feature.live.client.domain.UpdatePresentation
import feature.live.server.services.PresenterApi
import feature.live.server.services.QuestionApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import platform.client.http.ApiException

internal class PresenterRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: PresenterTokenStorage,
    private val scope: CoroutineScope,
) {
    private val isPresenter = tokenStorage.token
        .map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, tokenStorage.token.value != null)

    val flowOfIsPresenter = FlowOfIsPresenter { isPresenter }

    val signInPresenter = SignInPresenter { password ->
        val response = try {
            httpClient.post(PresenterApi.Session()) {
                setBody(PresenterApi.SignInRequest(password))
            }.body<PresenterApi.SignInResponse>()
        } catch (failure: ApiException) {
            if (failure.isUnauthorized) throw SignInPresenter.PasswordRejectedException()
            throw failure
        }
        tokenStorage.setToken(response.token)
    }

    val signOutPresenter = SignOutPresenter {
        val token = tokenStorage.token.value ?: return@SignOutPresenter
        tokenStorage.setToken(null)
        runCatching { httpClient.delete(PresenterApi.Session()) { bearerAuth(token) } }
    }

    val updatePresentation = UpdatePresentation { update ->
        asPresenter {
            when (update) {
                is UpdatePresentation.Update.MoveTo -> httpClient.post(PresenterApi.Position()) {
                    authorised()
                    setBody(update.position)
                }
                is UpdatePresentation.Update.SetLive -> httpClient.post(PresenterApi.Live()) {
                    authorised()
                    setBody(PresenterApi.SetLiveRequest(update.isLive))
                }
            }
        }
    }

    val hideQuestion = HideQuestion { id ->
        asPresenter {
            httpClient.post(QuestionApi.Hide(id)) { authorised() }
        }
    }

    val resetAudience = ResetAudience {
        asPresenter {
            httpClient.post(PresenterApi.Reset()) { authorised() }
        }
    }

    /** A 401 means the session has expired or been revoked, so this browser stops presenting. */
    private suspend fun <T> asPresenter(block: suspend () -> T): T {
        return try {
            block()
        } catch (failure: ApiException) {
            if (failure.isUnauthorized) tokenStorage.setToken(null)
            throw failure
        }
    }

    private fun HttpRequestBuilder.authorised() {
        tokenStorage.token.value?.let(::bearerAuth)
    }
}
