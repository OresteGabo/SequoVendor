package dev.orestegabo.sequovendor.core.notifications

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType

interface VendorNotificationsRepository {
    suspend fun registerFcmToken(deviceId: String, fcmToken: String, platform: String): ApiResult<Unit>
    suspend fun revokeDevice(deviceId: String): ApiResult<Unit>
    suspend fun inbox(includeArchived: Boolean = false): ApiResult<List<VendorNotification>>
    suspend fun markRead(messageId: String): ApiResult<VendorNotification>
    suspend fun archive(messageId: String): ApiResult<VendorNotification>
    suspend fun unarchive(messageId: String): ApiResult<VendorNotification>
    suspend fun effectivePreference(eventType: VendorNotificationPreferenceEventType): ApiResult<VendorNotificationPreference>
    suspend fun savePreference(preference: VendorNotificationPreference): ApiResult<VendorNotificationPreference>
}

class NetworkVendorNotificationsRepository(
    private val apiClient: NotificationsApiClient,
) : VendorNotificationsRepository {
    @Suppress("UNUSED_EXPRESSION")
    override suspend fun registerFcmToken(deviceId: String, fcmToken: String, platform: String): ApiResult<Unit> =
        safeApiCall {
            apiClient.registerFcmToken(deviceId, fcmToken, platform).let { Unit }
        }

    override suspend fun revokeDevice(deviceId: String): ApiResult<Unit> =
        safeApiCall { apiClient.revokeDevice(deviceId) }

    override suspend fun inbox(includeArchived: Boolean): ApiResult<List<VendorNotification>> =
        safeApiCall { apiClient.inbox(includeArchived = includeArchived) }

    override suspend fun markRead(messageId: String): ApiResult<VendorNotification> =
        safeApiCall { apiClient.markRead(messageId) }

    override suspend fun archive(messageId: String): ApiResult<VendorNotification> =
        safeApiCall { apiClient.archive(messageId) }

    override suspend fun unarchive(messageId: String): ApiResult<VendorNotification> =
        safeApiCall { apiClient.unarchive(messageId) }

    override suspend fun effectivePreference(eventType: VendorNotificationPreferenceEventType): ApiResult<VendorNotificationPreference> =
        safeApiCall { apiClient.effectivePreference(eventType.toApiEventType()) }

    override suspend fun savePreference(preference: VendorNotificationPreference): ApiResult<VendorNotificationPreference> =
        safeApiCall { apiClient.savePreference(preference) }
}

class FakeVendorNotificationsRepository(
    initialNotifications: List<VendorNotification>,
) : VendorNotificationsRepository {
    private var messages = initialNotifications
    private var preference = VendorNotificationPreference()

    override suspend fun registerFcmToken(deviceId: String, fcmToken: String, platform: String): ApiResult<Unit> =
        ApiResult.Success(Unit)

    override suspend fun revokeDevice(deviceId: String): ApiResult<Unit> =
        ApiResult.Success(Unit)

    override suspend fun inbox(includeArchived: Boolean): ApiResult<List<VendorNotification>> =
        ApiResult.Success(messages.filter { includeArchived || !it.archived })

    override suspend fun markRead(messageId: String): ApiResult<VendorNotification> =
        update(messageId) { it.copy(readAt = it.readAt ?: "Local update") }

    override suspend fun archive(messageId: String): ApiResult<VendorNotification> =
        update(messageId) { it.copy(readAt = it.readAt ?: "Local update", archivedAt = it.archivedAt ?: "Local update") }

    override suspend fun unarchive(messageId: String): ApiResult<VendorNotification> =
        update(messageId) { it.copy(archivedAt = null) }

    override suspend fun effectivePreference(eventType: VendorNotificationPreferenceEventType): ApiResult<VendorNotificationPreference> =
        ApiResult.Success(preference.copy(eventType = eventType))

    override suspend fun savePreference(preference: VendorNotificationPreference): ApiResult<VendorNotificationPreference> {
        this.preference = preference
        return ApiResult.Success(preference)
    }

    private fun update(
        messageId: String,
        transform: (VendorNotification) -> VendorNotification,
    ): ApiResult<VendorNotification> {
        val updated = transform(messages.first { it.id == messageId })
        messages = messages.map { if (it.id == messageId) updated else it }
        return ApiResult.Success(updated)
    }
}
