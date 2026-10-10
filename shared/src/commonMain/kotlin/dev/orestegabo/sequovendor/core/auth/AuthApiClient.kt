package dev.orestegabo.sequovendor.core.auth

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.SequoVendorApiException
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.core.network.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.SerialName

class AuthApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
    private val deviceIdProvider: () -> String,
) {
    suspend fun loginWithGoogle(idToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Google, idToken)
    }

    suspend fun loginWithFacebook(accessToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Facebook, accessToken)
    }

    private suspend fun loginWithSocialToken(provider: SocialLoginProvider, token: String): AuthApiResponse {
        val response = try {
            httpClient.post(NetworkConfig.apiUrl("auth/social/${provider.wireName}", baseUrl)) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    SocialLoginRequest(
                        token = token,
                        device = DeviceRequest(
                            deviceId = deviceIdProvider(),
                            appSource = AppSource.SequoHub,
                        ),
                    ),
                )
            }
        } catch (error: SequoVendorApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = socialLoginFailureMessage(provider, error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse(provider)
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthApiResponse {
        val response = try {
            httpClient.post(NetworkConfig.apiUrl("auth/signup", baseUrl)) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(EmailSignUpRequest(email = email, password = password, name = name?.takeIf { it.isNotBlank() }))
            }
        } catch (error: SequoVendorApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
    }

    suspend fun loginWithEmail(email: String, password: String): AuthApiResponse {
        val response = try {
            httpClient.post(NetworkConfig.apiUrl("auth/login", baseUrl)) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(EmailLoginRequest(email = email, password = password))
            }
        } catch (error: SequoVendorApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
    }

    suspend fun currentUser(accessToken: String): CurrentUser {
        return try {
            httpClient.get(NetworkConfig.apiUrl("auth/me", baseUrl)) {
                bearerAuth(accessToken)
            }.body()
        } catch (error: SequoVendorApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = AuthUserMessage.ProfileLoadFailed,
                cause = error,
            )
        }
    }

    suspend fun refreshSession(refreshToken: String): AuthSession {
        return try {
            httpClient.post(NetworkConfig.apiUrl("auth/refresh", baseUrl)) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(RefreshRequest(refreshToken = refreshToken, deviceId = deviceIdProvider()))
            }.body()
        } catch (error: SequoVendorApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = AuthUserMessage.SessionRestoreFailed,
                cause = error,
            )
        }
    }

    suspend fun logout(refreshToken: String) {
        try {
            httpClient.post(NetworkConfig.apiUrl("auth/logout", baseUrl)) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(LogoutRequest(refreshToken))
            }
        } catch (_: SequoVendorApiException) {
            // Local logout must still complete when the network is unavailable.
        }
    }

    fun close() {
        httpClient.close()
    }

    private suspend fun HttpResponse.toAuthApiResponse(provider: SocialLoginProvider = SocialLoginProvider.Google): AuthApiResponse {
        val statusCode = status.value
        if (statusCode !in 200..299) {
            throw AuthApiException(
                statusCode = statusCode,
                userMessage = socialLoginFailureMessage(provider, statusCode, bodyAsText()),
            )
        }

        return AuthApiResponse(
            statusCode = statusCode,
            session = body(),
        )
    }
}

private fun emailAuthFailureMessage(statusCode: Int, @Suppress("UNUSED_PARAMETER") responseBody: String?): AuthUserMessage {
    return when (statusCode) {
        400 -> AuthUserMessage.EmailFormatInvalid
        401 -> AuthUserMessage.EmailPasswordIncorrect
        409 -> AuthUserMessage.AccountUsesAnotherMethod
        429 -> AuthUserMessage.TooManyAttempts
        else -> AuthUserMessage.AuthenticationUnavailable
    }
}

private fun socialLoginFailureMessage(provider: SocialLoginProvider, statusCode: Int, responseBody: String?): AuthUserMessage {
    if (provider != SocialLoginProvider.Google) {
        return when (statusCode) {
            401 -> AuthUserMessage.SocialSignInFailed
            409 -> AuthUserMessage.AccountUsesAnotherMethod
            429 -> AuthUserMessage.TooManyAttempts
            else -> AuthUserMessage.SocialSignInUnavailable
        }
    }

    val googleError = responseBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body ->
            runCatching { defaultNetworkJson.decodeFromString<GoogleLoginErrorResponse>(body) }.getOrNull()
        }

    return when (googleError?.reason ?: googleError?.code ?: googleError?.error) {
        "forbidden", "access_denied" -> {
            AuthUserMessage.GoogleSignInUnavailable
        }
        "invalid_audience" -> {
            AuthUserMessage.GoogleSignInTemporarilyUnavailable
        }
        "missing_allowed_audience" -> {
            AuthUserMessage.GoogleSignInTemporarilyUnavailable
        }
        "expired_token" -> AuthUserMessage.GoogleSignInExpired
        "missing_email" -> AuthUserMessage.GoogleMissingEmail
        "unverified_email" -> AuthUserMessage.GoogleEmailUnverified
        "invalid_issuer", "invalid_signature", "malformed_token", "invalid_token" -> {
            AuthUserMessage.GoogleSignInCouldNotVerify
        }
        "account_link_required" -> {
            AuthUserMessage.AccountUsesAnotherMethod
        }
        else -> when {
            statusCode == 403 -> AuthUserMessage.GoogleSignInUnavailable
            else -> AuthUserMessage.GoogleSignInUnavailable
        }
    }
}

data class AuthApiResponse(
    val statusCode: Int,
    val session: AuthSession,
)

@Serializable
data class CurrentUser(
    val id: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val name: String? = null,
    @SerialName("displayName")
    val backendDisplayName: String? = null,
    val provider: String,
    val status: String,
    val active: Boolean = true,
    val roles: Set<String> = emptySet(),
) {
    val displayName: String
        get() = backendDisplayName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Sequo merchant"
}

fun CurrentUser.hasMerchantAccess(): Boolean =
    active && roles.any { it.uppercase() in MerchantRoles }

private val MerchantRoles = setOf(
    "MERCHANT_OWNER",
    "MERCHANT_STAFF",
    "ADMIN",
    "SUPER_ADMIN",
)

class AuthApiException(
    val statusCode: Int,
    val userMessage: AuthUserMessage,
    override val cause: Throwable? = null,
) : Exception(userMessage.name, cause)

enum class AuthUserMessage {
    EmailFormatInvalid,
    EmailPasswordIncorrect,
    AccountUsesAnotherMethod,
    TooManyAttempts,
    AuthenticationUnavailable,
    SocialSignInFailed,
    SocialSignInUnavailable,
    GoogleSignInUnavailable,
    GoogleSignInTemporarilyUnavailable,
    GoogleSignInExpired,
    GoogleMissingEmail,
    GoogleEmailUnverified,
    GoogleSignInCouldNotVerify,
    ProfileLoadFailed,
    SessionRestoreFailed,
    MerchantRoleRequired,
}

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
private data class SocialLoginRequest(
    val token: String,
    val device: DeviceRequest,
)

@Serializable
private data class DeviceRequest(
    val deviceId: String,
    val appSource: AppSource,
)

@Serializable
private data class EmailSignUpRequest(
    val email: String,
    val password: String,
    val name: String? = null,
)

@Serializable
private data class EmailLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String,
)

@Serializable
private data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
private data class AuthErrorResponse(
    val code: String? = null,
    val message: String? = null,
    val requiredProvider: String? = null,
)

@Serializable
private data class GoogleLoginErrorResponse(
    val error: String? = null,
    val reason: String? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
private enum class SocialLoginProvider {
    @SerialName("GOOGLE")
    Google,

    @SerialName("FACEBOOK")
    Facebook;

    val wireName: String
        get() = when (this) {
            Google -> "GOOGLE"
            Facebook -> "FACEBOOK"
        }

    val displayName: String
        get() = when (this) {
            Google -> "Google"
            Facebook -> "Facebook"
        }
}

@Serializable
private enum class AppSource {
    @SerialName("SEQUO_HUB")
    SequoHub,
}
