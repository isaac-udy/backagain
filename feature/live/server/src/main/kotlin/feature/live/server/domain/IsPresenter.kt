package feature.live.server.domain

fun interface IsPresenter {
    /** True when [token] belongs to an unexpired presenter session. */
    suspend operator fun invoke(token: String?): Boolean
}
