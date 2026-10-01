package platform.client.http

import platform.http.ApiError

/** A non-2xx response from the API, carrying the server's [ApiError] when it sent one. */
class ApiException(
    val status: Int,
    val error: ApiError?,
) : RuntimeException(error?.message ?: "HTTP $status") {

    val isUnauthorized: Boolean get() = status == 401

    val isRateLimited: Boolean get() = status == 429
}
