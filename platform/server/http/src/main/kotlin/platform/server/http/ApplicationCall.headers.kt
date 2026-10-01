package platform.server.http

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import platform.http.ApiHeaders

private val clientIdPattern = Regex("[A-Za-z0-9-]{8,64}")

/** @throws ApiException when the request carries no usable client id. */
fun ApplicationCall.clientId(): String {
    val clientId = request.headers[ApiHeaders.CLIENT_ID]
    if (clientId == null || !clientIdPattern.matches(clientId)) {
        throw ApiException.badRequest("client_id", "Missing or malformed ${ApiHeaders.CLIENT_ID} header")
    }
    return clientId
}

fun ApplicationCall.bearerToken(): String? =
    request.headers[HttpHeaders.Authorization]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substring("Bearer ".length)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
