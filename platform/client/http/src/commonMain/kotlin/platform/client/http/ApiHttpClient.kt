package platform.client.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import platform.http.ApiError
import platform.http.ApiHeaders
import platform.http.ApiJson

/**
 * The client every Repository talks to the server through. Requests are relative to
 * [ApiEndpoint.origin], send JSON, carry the device's client id, and turn any non-2xx response
 * into an [ApiException].
 */
fun ApiHttpClient(
    engine: HttpClientEngine,
    endpoint: ApiEndpoint,
    clientId: ClientId,
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(Resources)
    install(ContentNegotiation) {
        json(ApiJson)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(ApiJson)
    }
    defaultRequest {
        url(endpoint.origin)
        contentType(ContentType.Application.Json)
        headers.append(ApiHeaders.CLIENT_ID, clientId.value)
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val response = when (cause) {
                is ClientRequestException -> cause.response
                is ServerResponseException -> cause.response
                else -> return@handleResponseExceptionWithRequest
            }
            val error = runCatching { ApiJson.decodeFromString<ApiError>(response.bodyAsText()) }.getOrNull()
            throw ApiException(status = response.status.value, error = error)
        }
    }
}
