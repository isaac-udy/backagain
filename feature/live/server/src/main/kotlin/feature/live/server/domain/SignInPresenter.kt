package feature.live.server.domain

fun interface SignInPresenter {
    /** @return a session token, or null when [password] is wrong. */
    suspend operator fun invoke(password: String): String?
}
