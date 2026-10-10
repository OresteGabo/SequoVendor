package dev.orestegabo.sequovendor.core.network

import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.error_generic_detail
import sequovendor.shared.generated.resources.error_not_found_detail
import sequovendor.shared.generated.resources.error_offline_detail
import sequovendor.shared.generated.resources.error_permission_detail
import sequovendor.shared.generated.resources.error_rate_limited_detail
import sequovendor.shared.generated.resources.error_server_detail
import sequovendor.shared.generated.resources.error_session_detail

fun createSequoVendorHttpClient(
    baseUrl: String = NetworkConfig.ProductionBaseUrl,
    json: Json = defaultNetworkJson,
    accessTokenProvider: (() -> String?)? = null,
    refreshAccessToken: (suspend () -> Boolean)? = null,
): HttpClient = HttpClient {
    expectSuccess = true

    defaultRequest {
        url.takeFrom(baseUrl)
        headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        accept(ContentType.Application.Json)
        accessTokenProvider?.invoke()?.takeIf { it.isNotBlank() }?.let { bearerAuth(it) }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }

    install(ContentNegotiation) {
        json(json)
    }

    if (accessTokenProvider != null && refreshAccessToken != null) {
        install(bearerRefreshRetryPlugin(accessTokenProvider, refreshAccessToken))
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            when (cause) {
                is ClientRequestException -> throw SequoVendorApiException(
                    statusCode = cause.response.status.value,
                    responseBody = cause.response.bodyAsText(),
                    retryAfterMillis = retryAfterDelayMillis(cause.response.headers[HttpHeaders.RetryAfter]),
                    message = "Sequo API rejected the request.",
                    cause = cause,
                )
                is ServerResponseException -> throw SequoVendorApiException(
                    statusCode = cause.response.status.value,
                    responseBody = cause.response.bodyAsText(),
                    retryAfterMillis = retryAfterDelayMillis(cause.response.headers[HttpHeaders.RetryAfter]),
                    message = "Sequo API failed.",
                    cause = cause,
                )
                is HttpRequestTimeoutException -> throw SequoVendorNetworkException(
                    message = "Sequo API request timed out.",
                    cause = cause,
                )
            }
        }
    }
}

private fun bearerRefreshRetryPlugin(
    accessTokenProvider: () -> String?,
    refreshAccessToken: suspend () -> Boolean,
) = createClientPlugin("SequoVendorBearerRefreshRetry") {
    on(Send) { request ->
        val firstCall = proceed(request)
        if (firstCall.response.status != HttpStatusCode.Unauthorized || refreshAccessToken() != true) {
            return@on firstCall
        }

        val retryRequest = HttpRequestBuilder().takeFrom(request)
        retryRequest.headers.remove(HttpHeaders.Authorization)
        accessTokenProvider().takeIf { !it.isNullOrBlank() }?.let { retryRequest.bearerAuth(it) }
        proceed(retryRequest)
    }
}

val defaultNetworkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

class SequoVendorApiException(
    val statusCode: Int,
    val responseBody: String?,
    val retryAfterMillis: Long? = null,
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)

class SequoVendorNetworkException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiFailure) : ApiResult<Nothing>
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Content<T>(val value: T) : UiState<T>
    data class Failed(val error: ApiFailure) : UiState<Nothing>
}

data class ApiFailure(
    val kind: ApiFailureKind,
    val statusCode: Int? = null,
    val retryAfterMillis: Long? = null,
) {
    val userMessage: StringResource
        get() = kind.userMessage
}

enum class ApiFailureKind(val userMessage: StringResource) {
    NetworkUnavailable(Res.string.error_offline_detail),
    SessionExpired(Res.string.error_session_detail),
    PermissionDenied(Res.string.error_permission_detail),
    NotFound(Res.string.error_not_found_detail),
    RateLimited(Res.string.error_rate_limited_detail),
    ServerUnavailable(Res.string.error_server_detail),
    Unexpected(Res.string.error_generic_detail),
}

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (error: Throwable) {
        ApiResult.Failure(error.toApiFailure())
    }

fun <T> ApiResult<T>.toUiState(isEmpty: (T) -> Boolean = { false }): UiState<T> =
    when (this) {
        is ApiResult.Success -> if (isEmpty(value)) UiState.Empty else UiState.Content(value)
        is ApiResult.Failure -> UiState.Failed(error)
    }

fun Throwable.toApiFailure(): ApiFailure =
    when (this) {
        is SequoVendorApiException -> ApiFailure(
            kind = when (statusCode) {
                401 -> ApiFailureKind.SessionExpired
                403 -> ApiFailureKind.PermissionDenied
                404 -> ApiFailureKind.NotFound
                429 -> ApiFailureKind.RateLimited
                in 500..599 -> ApiFailureKind.ServerUnavailable
                else -> ApiFailureKind.Unexpected
            },
            statusCode = statusCode,
            retryAfterMillis = retryAfterMillis,
        )
        is SequoVendorNetworkException,
        is HttpRequestTimeoutException,
        is ConnectTimeoutException,
        is SocketTimeoutException,
        -> ApiFailure(ApiFailureKind.NetworkUnavailable)
        else -> ApiFailure(ApiFailureKind.Unexpected)
    }

fun retryAfterDelayMillis(retryAfterHeader: String?): Long? {
    val seconds = retryAfterHeader
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.toLongOrNull()
        ?: return null

    return seconds.coerceAtLeast(0L) * 1_000L
}
