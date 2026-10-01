package feature.live.client.domain

import kotlin.coroutines.cancellation.CancellationException

fun interface SignInPresenter {
    @Throws(PasswordRejectedException::class, CancellationException::class)
    suspend operator fun invoke(password: String)

    class PasswordRejectedException : RuntimeException("That password isn't right")
}
