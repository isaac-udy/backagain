package platform.http

object ApiHeaders {
    /**
     * An anonymous id each browser generates once and keeps, so the server can rate-limit and
     * de-duplicate per device without anyone signing in.
     */
    const val CLIENT_ID: String = "X-Client-Id"
}
