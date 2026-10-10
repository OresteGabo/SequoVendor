package dev.orestegabo.sequovendor.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class ApiHealthClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
    private val json: Json = defaultNetworkJson,
) {
    suspend fun checkHealth(path: String = "/actuator/health"): ApiHealthStatus =
        runCatching {
            val response = httpClient.get("${baseUrl.trimEnd('/')}/${path.trimStart('/')}")
            val status = response.bodyAsText().actuatorStatusOrNull()
            ApiHealthStatus(
                reachedBackend = true,
                healthy = response.status.value in 200..299,
                statusCode = response.status.value,
                actuatorStatus = status,
            )
        }.getOrElse {
            ApiHealthStatus(
                reachedBackend = false,
                healthy = false,
            )
        }

    private fun String.actuatorStatusOrNull(): String? =
        try {
            json.decodeFromString<ActuatorHealthResponse>(this).status
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}

data class ApiHealthStatus(
    val reachedBackend: Boolean,
    val healthy: Boolean,
    val statusCode: Int? = null,
    val actuatorStatus: String? = null,
)

@Serializable
private data class ActuatorHealthResponse(
    val status: String? = null,
)
