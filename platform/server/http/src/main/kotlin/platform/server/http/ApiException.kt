package platform.server.http

import io.ktor.http.HttpStatusCode

/** Thrown from a route to answer with [status] and an `ApiError` body; see [apiErrors]. */
class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message) {

    companion object {
        fun badRequest(code: String, message: String) =
            ApiException(HttpStatusCode.BadRequest, code, message)

        fun unauthorized(message: String) =
            ApiException(HttpStatusCode.Unauthorized, "unauthorized", message)

        fun notFound(message: String) =
            ApiException(HttpStatusCode.NotFound, "not_found", message)
    }
}
