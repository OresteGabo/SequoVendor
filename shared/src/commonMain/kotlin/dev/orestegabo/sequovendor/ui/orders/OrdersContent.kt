package dev.orestegabo.sequovendor.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.order_net
import sequovendor.shared.generated.resources.order_packages
import sequovendor.shared.generated.resources.order_sla
import sequovendor.shared.generated.resources.order_status_accepted
import sequovendor.shared.generated.resources.order_status_cancelled
import sequovendor.shared.generated.resources.order_status_handed_off
import sequovendor.shared.generated.resources.order_status_packed
import sequovendor.shared.generated.resources.order_status_pending
import sequovendor.shared.generated.resources.order_status_preparing
import sequovendor.shared.generated.resources.order_status_rejected
import sequovendor.shared.generated.resources.orders_detail
import sequovendor.shared.generated.resources.orders_empty
import sequovendor.shared.generated.resources.orders_title

@Composable
internal fun OrdersContent(
    orders: List<MerchantSubOrder>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.orders_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.orders_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (orders.isEmpty()) {
            item {
                VendorCard {
                    Text(appText(Res.string.orders_empty), modifier = Modifier.padding(18.dp))
                }
            }
        }
        items(orders, key = { it.id }) { order ->
            OrderCard(order)
        }
    }
}

@Composable
private fun OrderCard(order: MerchantSubOrder) {
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(order.status.label(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            KeyValueRow(appText(Res.string.order_net), "CFA ${order.merchantNetCfa}")
            KeyValueRow(appText(Res.string.order_packages), order.packageCount.toString())
            KeyValueRow(appText(Res.string.order_sla), order.packingDueAt ?: order.sellerResponseDueAt ?: "-")
        }
    }
}

@Composable
private fun MerchantSubOrderStatus.label(): String = when (this) {
    MerchantSubOrderStatus.MerchantPending -> appText(Res.string.order_status_pending)
    MerchantSubOrderStatus.Accepted -> appText(Res.string.order_status_accepted)
    MerchantSubOrderStatus.Preparing -> appText(Res.string.order_status_preparing)
    MerchantSubOrderStatus.PackedReady -> appText(Res.string.order_status_packed)
    MerchantSubOrderStatus.HandedToCourier -> appText(Res.string.order_status_handed_off)
    MerchantSubOrderStatus.Rejected -> appText(Res.string.order_status_rejected)
    MerchantSubOrderStatus.Cancelled -> appText(Res.string.order_status_cancelled)
}
