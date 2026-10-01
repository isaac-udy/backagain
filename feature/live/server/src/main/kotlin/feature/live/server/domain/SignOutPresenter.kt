package feature.live.server.domain

fun interface SignOutPresenter {
    suspend operator fun invoke(token: String)
}
