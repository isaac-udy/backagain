package platform.server.http

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.path
import io.ktor.server.response.respond
import platform.http.ApiError

/**
 * Maps failures to a status and an [ApiError] body. A domain rule broken by the request — a
 * `require` that fails — is the caller's error, so it answers 400 with the rule's message.
 */
fun StatusPagesConfig.apiErrors() {
    exception<ApiException> { call, cause ->
        call.respond(cause.status, ApiError(code = cause.code, message = cause.message))
    }
    exception<IllegalArgumentException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ApiError(code = "invalid", message = cause.message ?: "Invalid request"))
    }
    exception<BadRequestException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ApiError(code = "bad_request", message = cause.message ?: "Bad request"))
    }
    exception<Throwable> { call, cause ->
        call.application.log.error("Unhandled error on ${call.request.path()}", cause)
        call.respond(HttpStatusCode.InternalServerError, ApiError(code = "internal", message = "Something went wrong"))
    }
    status(HttpStatusCode.TooManyRequests) { call, status ->
        call.respond(status, ApiError(code = "rate_limited", message = "Slow down a little"))
    }
}
