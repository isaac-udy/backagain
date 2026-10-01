package platform.client.http

/**
 * Where the API lives. The web client is served by the same server it calls, so this is the
 * page's own origin: no CORS, and no URL to configure per environment.
 */
data class ApiEndpoint(val origin: String) {
    val socketOrigin: String get() = origin.replaceFirst("http", "ws")
}
