package dev.orestegabo.sequovendor.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationEventType
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorErrorKind
import dev.orestegabo.sequovendor.ui.components.VendorErrorPanel
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.notifications_action_failed
import sequovendor.shared.generated.resources.notifications_archive
import sequovendor.shared.generated.resources.notifications_delivery_problems
import sequovendor.shared.generated.resources.notifications_detail
import sequovendor.shared.generated.resources.notifications_empty
import sequovendor.shared.generated.resources.notifications_in_app
import sequovendor.shared.generated.resources.notifications_loading
import sequovendor.shared.generated.resources.notifications_mark_read
import sequovendor.shared.generated.resources.notifications_new_orders
import sequovendor.shared.generated.resources.notifications_preferences
import sequovendor.shared.generated.resources.notifications_preference_saved
import sequovendor.shared.generated.resources.notifications_push
import sequovendor.shared.generated.resources.notifications_refresh
import sequovendor.shared.generated.resources.notifications_save_preferences
import sequovendor.shared.generated.resources.notifications_show_archived
import sequovendor.shared.generated.resources.notifications_sla_warnings
import sequovendor.shared.generated.resources.notifications_sms
import sequovendor.shared.generated.resources.notifications_title
import sequovendor.shared.generated.resources.notifications_unarchive

@Composable
internal fun NotificationsContent(
    notifications: List<VendorNotification>,
    preference: VendorNotificationPreference,
    isLoading: Boolean,
    isOffline: Boolean,
    actionFailed: Boolean,
    preferenceSaved: Boolean,
    includeArchived: Boolean,
    onIncludeArchivedChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onMarkRead: (VendorNotification) -> Unit,
    onArchive: (VendorNotification) -> Unit,
    onUnarchive: (VendorNotification) -> Unit,
    onPreferenceChange: (VendorNotificationPreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = notifications.filter { includeArchived || !it.archived }
    val newOrders = visible.filter { it.eventType in NewOrderEvents }
    val slaWarnings = visible.filter { it.eventType == VendorNotificationEventType.MERCHANT_SLA_WARNING }
    val deliveryProblems = visible.filter { it.eventType in DeliveryProblemEvents }
    val newOrdersTitle = appText(Res.string.notifications_new_orders)
    val slaWarningsTitle = appText(Res.string.notifications_sla_warnings)
    val deliveryProblemsTitle = appText(Res.string.notifications_delivery_problems)

    LazyColumn(
        modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.notifications_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.notifications_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (actionFailed) Text(appText(Res.string.notifications_action_failed), color = MaterialTheme.colorScheme.error)
                if (preferenceSaved) Text(appText(Res.string.notifications_preference_saved), color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isOffline) {
            item { VendorErrorPanel(VendorErrorKind.Offline, onRetry = onRefresh) }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = includeArchived, onCheckedChange = onIncludeArchivedChange)
                            Text(appText(Res.string.notifications_show_archived))
                        }
                        Button(onClick = onRefresh, enabled = !isLoading) {
                            Text(appText(Res.string.notifications_refresh))
                        }
                    }
                    if (isLoading) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Text(appText(Res.string.notifications_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            NotificationPreferenceCard(
                preference = preference,
                onPreferenceChange = onPreferenceChange,
            )
        }

        notificationSection(
            title = newOrdersTitle,
            notifications = newOrders,
            emptyAllowed = visible.isEmpty(),
            onMarkRead = onMarkRead,
            onArchive = onArchive,
            onUnarchive = onUnarchive,
        )
        notificationSection(
            title = slaWarningsTitle,
            notifications = slaWarnings,
            emptyAllowed = false,
            onMarkRead = onMarkRead,
            onArchive = onArchive,
            onUnarchive = onUnarchive,
        )
        notificationSection(
            title = deliveryProblemsTitle,
            notifications = deliveryProblems,
            emptyAllowed = false,
            onMarkRead = onMarkRead,
            onArchive = onArchive,
            onUnarchive = onUnarchive,
        )
    }
}

private fun LazyListScope.notificationSection(
    title: String,
    notifications: List<VendorNotification>,
    emptyAllowed: Boolean,
    onMarkRead: (VendorNotification) -> Unit,
    onArchive: (VendorNotification) -> Unit,
    onUnarchive: (VendorNotification) -> Unit,
) {
    item {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    if (notifications.isEmpty() && emptyAllowed) {
        item {
            VendorCard {
                Text(appText(Res.string.notifications_empty), modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    items(notifications, key = { it.id }) { notification ->
        NotificationCard(
            notification = notification,
            onMarkRead = onMarkRead,
            onArchive = onArchive,
            onUnarchive = onUnarchive,
        )
    }
}

@Composable
private fun NotificationPreferenceCard(
    preference: VendorNotificationPreference,
    onPreferenceChange: (VendorNotificationPreference) -> Unit,
) {
    var pushEnabled by remember(preference) { mutableStateOf(preference.pushEnabled) }
    var inAppEnabled by remember(preference) { mutableStateOf(preference.inAppEnabled) }
    var smsEnabled by remember(preference) { mutableStateOf(preference.smsEnabled) }

    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(appText(Res.string.notifications_preferences), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            PreferenceSwitch(appText(Res.string.notifications_push), pushEnabled) { pushEnabled = it }
            PreferenceSwitch(appText(Res.string.notifications_in_app), inAppEnabled) { inAppEnabled = it }
            PreferenceSwitch(appText(Res.string.notifications_sms), smsEnabled) { smsEnabled = it }
            Button(
                onClick = {
                    onPreferenceChange(
                        preference.copy(
                            eventType = VendorNotificationPreferenceEventType.ALL,
                            pushEnabled = pushEnabled,
                            inAppEnabled = inAppEnabled,
                            smsEnabled = smsEnabled,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(appText(Res.string.notifications_save_preferences))
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NotificationCard(
    notification: VendorNotification,
    onMarkRead: (VendorNotification) -> Unit,
    onArchive: (VendorNotification) -> Unit,
    onUnarchive: (VendorNotification) -> Unit,
) {
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(notification.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(notification.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(notification.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(notification.createdAt.formatIsoTimestampOrDash(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onMarkRead(notification) }, enabled = notification.unread, modifier = Modifier.weight(1f)) {
                    Text(appText(Res.string.notifications_mark_read))
                }
                OutlinedButton(
                    onClick = { if (notification.archived) onUnarchive(notification) else onArchive(notification) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (notification.archived) appText(Res.string.notifications_unarchive) else appText(Res.string.notifications_archive))
                }
            }
        }
    }
}

private fun VendorNotification.icon(): ImageVector =
    when (eventType) {
        VendorNotificationEventType.ORDER_CREATED -> Icons.Filled.Schedule
        VendorNotificationEventType.MERCHANT_SLA_WARNING -> Icons.Filled.Warning
        VendorNotificationEventType.ORDER_READY_FOR_PICKUP -> Icons.Filled.CheckCircle
        VendorNotificationEventType.DELIVERY_PROBLEM_REPORTED,
        VendorNotificationEventType.MISSING_DEPOT_BLOCKED_TOUR,
        -> Icons.Filled.LocalShipping
        else -> Icons.Filled.NotificationsActive
    }

private val NewOrderEvents = setOf(
    VendorNotificationEventType.ORDER_CREATED,
    VendorNotificationEventType.PAYMENT_CONFIRMED,
)

private val DeliveryProblemEvents = setOf(
    VendorNotificationEventType.DELIVERY_PROBLEM_REPORTED,
    VendorNotificationEventType.MISSING_DEPOT_BLOCKED_TOUR,
    VendorNotificationEventType.RELAY_PARCEL_DELAYED,
)
