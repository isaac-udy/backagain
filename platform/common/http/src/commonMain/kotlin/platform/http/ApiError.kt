package platform.http

import kotlinx.serialization.Serializable

/** The body of every non-2xx API response. */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
)
