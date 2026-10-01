package feature.live.client.domain

fun interface SignOutPresenter {
    suspend operator fun invoke()
}
