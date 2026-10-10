package dev.orestegabo.sequovendor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.model.formatCfa
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.MetricTile
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.home_accept_orders
import sequovendor.shared.generated.resources.home_check_catalog
import sequovendor.shared.generated.resources.home_detail
import sequovendor.shared.generated.resources.home_next_actions
import sequovendor.shared.generated.resources.home_pack_orders
import sequovendor.shared.generated.resources.home_pending
import sequovendor.shared.generated.resources.home_picked_up
import sequovendor.shared.generated.resources.home_preparing
import sequovendor.shared.generated.resources.home_products_attention
import sequovendor.shared.generated.resources.home_ready
import sequovendor.shared.generated.resources.home_store_offline
import sequovendor.shared.generated.resources.home_store_online
import sequovendor.shared.generated.resources.home_title
import sequovendor.shared.generated.resources.home_today_net
import sequovendor.shared.generated.resources.order_status_packed
import sequovendor.shared.generated.resources.order_status_pending
import sequovendor.shared.generated.resources.order_status_preparing

@Composable
internal fun DashboardContent(
    session: VendorSession,
    orders: List<MerchantSubOrder>,
    products: List<CatalogProduct>,
    store: StoreAvailabilityState,
    onOrdersClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pending = orders.count { it.status == MerchantSubOrderStatus.MERCHANT_PENDING }
    val preparing = orders.count { it.status == MerchantSubOrderStatus.PREPARING || it.status == MerchantSubOrderStatus.ACCEPTED }
    val ready = orders.count { it.status == MerchantSubOrderStatus.PACKED_READY }
    val pickedUp = orders.count { it.status == MerchantSubOrderStatus.HANDED_TO_COURIER }
    val todayNet = orders
        .filter { it.status != MerchantSubOrderStatus.REJECTED && it.status != MerchantSubOrderStatus.CANCELLED }
        .sumOf { it.merchantNetCfa }
    val productsNeedingPhotos = products.count { !it.cameraVerified }

    LazyColumn(
        modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.home_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(session.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(appText(Res.string.home_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(appText(Res.string.home_pending), pending.toString(), Modifier.weight(1f))
                MetricTile(appText(Res.string.home_preparing), preparing.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(appText(Res.string.home_ready), ready.toString(), Modifier.weight(1f))
                MetricTile(appText(Res.string.home_picked_up), pickedUp.toString(), Modifier.weight(1f))
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(appText(Res.string.home_today_net), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(todayNet.formatCfa(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    KeyValueRow(appText(Res.string.home_products_attention), productsNeedingPhotos.toString())
                    KeyValueRow(
                        label = if (store.open) appText(Res.string.home_store_online) else appText(Res.string.home_store_offline),
                        value = store.openStatus ?: if (store.open) "OPEN" else "PAUSED",
                    )
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.home_next_actions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardActionRow(Icons.Filled.ReceiptLong, appText(Res.string.home_accept_orders), "${appText(Res.string.order_status_pending)}: $pending", onOrdersClick)
                    DashboardActionRow(Icons.Filled.CheckCircle, appText(Res.string.home_pack_orders), "${appText(Res.string.order_status_preparing)} / ${appText(Res.string.order_status_packed)}: ${preparing + ready}", onOrdersClick)
                    DashboardActionRow(Icons.Filled.Inventory2, appText(Res.string.home_check_catalog), productsNeedingPhotos.toString(), onCatalogClick)
                    DashboardActionRow(Icons.Filled.Storefront, if (store.open) appText(Res.string.home_store_online) else appText(Res.string.home_store_offline), store.weekdayHours, onStoreClick)
                }
            }
        }
    }
}

@Composable
private fun DashboardActionRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
