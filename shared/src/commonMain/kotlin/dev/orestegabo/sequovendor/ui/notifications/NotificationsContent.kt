package dev.orestegabo.sequovendor.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.notifications_empty
import sequovendor.shared.generated.resources.notifications_order_ready
import sequovendor.shared.generated.resources.notifications_pending_order
import sequovendor.shared.generated.resources.notifications_sla_due
import sequovendor.shared.generated.resources.notifications_title
import sequovendor.shared.generated.resources.notifications_detail

@Composable
internal fun NotificationsContent(
    orders: List<MerchantSubOrder>,
    modifier: Modifier = Modifier,
) {
    val notifications = orders
        .filter { it.status in NotificationStatuses }
        .map { it.toNotificationItem() }

    LazyColumn(
        modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.notifications_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.notifications_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (notifications.isEmpty()) {
            item {
                VendorCard {
                    Text(appText(Res.string.notifications_empty), modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(notifications, key = { it.id }) { item ->
            VendorCard {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(item.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class VendorNotificationItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val time: String,
)

@Composable
private fun MerchantSubOrder.toNotificationItem(): VendorNotificationItem =
    when (status) {
        MerchantSubOrderStatus.MERCHANT_PENDING -> VendorNotificationItem(
            id = "$id-pending",
            icon = Icons.Filled.Schedule,
            title = appText(Res.string.notifications_pending_order),
            detail = code,
            time = sellerResponseDueAt.formatIsoTimestampOrDash(),
        )
        MerchantSubOrderStatus.PACKED_READY -> VendorNotificationItem(
            id = "$id-ready",
            icon = Icons.Filled.CheckCircle,
            title = appText(Res.string.notifications_order_ready),
            detail = code,
            time = packedReadyAt.formatIsoTimestampOrDash(),
        )
        else -> VendorNotificationItem(
            id = "$id-sla",
            icon = Icons.Filled.Warning,
            title = appText(Res.string.notifications_sla_due),
            detail = code,
            time = (packingDueAt ?: sellerResponseDueAt).formatIsoTimestampOrDash(),
        )
    }

private val NotificationStatuses = setOf(
    MerchantSubOrderStatus.MERCHANT_PENDING,
    MerchantSubOrderStatus.ACCEPTED,
    MerchantSubOrderStatus.PREPARING,
    MerchantSubOrderStatus.PACKED_READY,
)
