package dev.orestegabo.sequovendor.core.notifications

import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationEventType
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType
import dev.orestegabo.sequovendor.model.VendorNotificationSeverity
import kotlinx.serialization.Serializable

internal const val SequoMerchantAppFamily = "SEQUO_MERCHANT"

@Serializable
data class RegisterFcmTokenRequestDto(
    val deviceId: String,
    val fcmToken: String,
    val appFamily: String = SequoMerchantAppFamily,
    val platform: String,
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
)

@Serializable
data class DeviceFcmTokenResponseDto(
    val id: String,
    val deviceId: String,
    val appFamily: String,
    val platform: String,
    val status: String,
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
    val lastSeenAt: String? = null,
)

@Serializable
data class NotificationMessageDto(
    val id: String,
    val eventId: String,
    val recipientUserId: String,
    val appFamily: String,
    val eventType: String,
    val severity: String,
    val title: String,
    val body: String,
    val actionUrl: String? = null,
    val payload: String? = null,
    val readAt: String? = null,
    val archivedAt: String? = null,
    val createdAt: String,
    val deliveries: List<NotificationDeliveryDto> = emptyList(),
    val smsSuppressedReason: String? = null,
)

@Serializable
data class NotificationDeliveryDto(
    val id: String,
    val messageId: String,
    val channel: String,
    val targetRef: String,
    val status: String,
    val attemptCount: Int,
    val sentAt: String? = null,
)

@Serializable
data class SaveNotificationPreferenceRequestDto(
    val eventType: String = "ALL",
    val pushEnabled: Boolean = true,
    val inAppEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
)

@Serializable
data class NotificationPreferenceDto(
    val id: String? = null,
    val appFamily: String = SequoMerchantAppFamily,
    val eventType: String = "ALL",
    val pushEnabled: Boolean = true,
    val inAppEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
)

fun NotificationMessageDto.toDomain(): VendorNotification =
    VendorNotification(
        id = id,
        eventId = eventId,
        appFamily = appFamily,
        eventType = eventType.toNotificationEventType(),
        severity = severity.toNotificationSeverity(),
        title = title,
        body = body,
        actionUrl = actionUrl,
        readAt = readAt,
        archivedAt = archivedAt,
        createdAt = createdAt,
    )

fun NotificationPreferenceDto.toDomain(): VendorNotificationPreference =
    VendorNotificationPreference(
        id = id,
        appFamily = appFamily,
        eventType = eventType.toNotificationPreferenceEventType(),
        pushEnabled = pushEnabled,
        inAppEnabled = inAppEnabled,
        smsEnabled = smsEnabled,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
    )

fun VendorNotificationPreference.toDto(): SaveNotificationPreferenceRequestDto =
    SaveNotificationPreferenceRequestDto(
        eventType = eventType.name,
        pushEnabled = pushEnabled,
        inAppEnabled = inAppEnabled,
        smsEnabled = smsEnabled,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
    )

private fun String.toNotificationEventType(): VendorNotificationEventType =
    runCatching { VendorNotificationEventType.valueOf(uppercase()) }
        .getOrDefault(VendorNotificationEventType.ORDER_CREATED)

private fun String.toNotificationSeverity(): VendorNotificationSeverity =
    runCatching { VendorNotificationSeverity.valueOf(uppercase()) }
        .getOrDefault(VendorNotificationSeverity.INFO)

private fun String.toNotificationPreferenceEventType(): VendorNotificationPreferenceEventType =
    runCatching { VendorNotificationPreferenceEventType.valueOf(uppercase()) }
        .getOrDefault(VendorNotificationPreferenceEventType.ALL)
