package dev.orestegabo.sequovendor.core.notifications

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NotificationsApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
) {
    suspend fun registerFcmToken(
        deviceId: String,
        fcmToken: String,
        platform: String,
        appVersion: String? = null,
        locale: String? = null,
        timezone: String? = null,
    ): DeviceFcmTokenResponseDto =
        httpClient.post(NetworkConfig.apiUrl("notifications/devices/fcm", baseUrl)) {
            setBody(
                RegisterFcmTokenRequestDto(
                    deviceId = deviceId,
                    fcmToken = fcmToken,
                    platform = platform,
                    appVersion = appVersion,
                    locale = locale,
                    timezone = timezone,
                ),
            )
        }.body()

    suspend fun revokeDevice(deviceId: String) {
        httpClient.delete(NetworkConfig.apiUrl("notifications/devices/$SequoMerchantAppFamily/$deviceId", baseUrl))
    }

    suspend fun inbox(
        includeArchived: Boolean = false,
        limit: Int = 50,
    ): List<VendorNotification> =
        httpClient.get(NetworkConfig.apiUrl("notifications/inbox", baseUrl)) {
            parameter("includeArchived", includeArchived)
            parameter("limit", limit)
        }.body<List<NotificationMessageDto>>()
            .filter { it.appFamily == SequoMerchantAppFamily }
            .map { it.toDomain() }

    suspend fun markRead(messageId: String): VendorNotification =
        httpClient.patch(NetworkConfig.apiUrl("notifications/inbox/$messageId/read", baseUrl))
            .body<NotificationMessageDto>()
            .toDomain()

    suspend fun archive(messageId: String): VendorNotification =
        httpClient.post(NetworkConfig.apiUrl("notifications/inbox/$messageId/archive", baseUrl))
            .body<NotificationMessageDto>()
            .toDomain()

    suspend fun unarchive(messageId: String): VendorNotification =
        httpClient.delete(NetworkConfig.apiUrl("notifications/inbox/$messageId/archive", baseUrl))
            .body<NotificationMessageDto>()
            .toDomain()

    suspend fun effectivePreference(eventType: String): VendorNotificationPreference =
        httpClient.get(NetworkConfig.apiUrl("notifications/preferences/$SequoMerchantAppFamily/effective", baseUrl)) {
            parameter("eventType", eventType)
        }.body<NotificationPreferenceDto>()
            .toDomain()

    suspend fun savePreference(preference: VendorNotificationPreference): VendorNotificationPreference =
        httpClient.put(NetworkConfig.apiUrl("notifications/preferences/$SequoMerchantAppFamily", baseUrl)) {
            setBody(preference.toDto())
        }.body<NotificationPreferenceDto>()
            .toDomain()
}

fun VendorNotificationPreferenceEventType.toApiEventType(): String =
    when (this) {
        VendorNotificationPreferenceEventType.ALL -> "ORDER_CREATED"
        else -> name
    }
