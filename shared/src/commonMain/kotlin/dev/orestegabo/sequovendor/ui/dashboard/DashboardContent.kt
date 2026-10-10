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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.MetricTile
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.dashboard_accepted
import sequovendor.shared.generated.resources.dashboard_api_connected
import sequovendor.shared.generated.resources.dashboard_api_connection
import sequovendor.shared.generated.resources.dashboard_api_error
import sequovendor.shared.generated.resources.dashboard_api_local
import sequovendor.shared.generated.resources.dashboard_close_store
import sequovendor.shared.generated.resources.dashboard_edit_catalog
import sequovendor.shared.generated.resources.dashboard_last_updated
import sequovendor.shared.generated.resources.dashboard_no_recent_orders
import sequovendor.shared.generated.resources.dashboard_no_sla_warnings
import sequovendor.shared.generated.resources.dashboard_open_store
import sequovendor.shared.generated.resources.dashboard_packing_due
import sequovendor.shared.generated.resources.dashboard_quick_actions
import sequovendor.shared.generated.resources.dashboard_recent_orders
import sequovendor.shared.generated.resources.dashboard_response_due
import sequovendor.shared.generated.resources.dashboard_sla_warnings
import sequovendor.shared.generated.resources.dashboard_store_status
import sequovendor.shared.generated.resources.dashboard_view_pending
import sequovendor.shared.generated.resources.home_detail
import sequovendor.shared.generated.resources.home_pending
import sequovendor.shared.generated.resources.home_products_attention
import sequovendor.shared.generated.resources.home_ready
import sequovendor.shared.generated.resources.home_store_offline
import sequovendor.shared.generated.resources.home_store_online
import sequovendor.shared.generated.resources.home_title
import sequovendor.shared.generated.resources.home_today_net
import sequovendor.shared.generated.resources.nav_store
import sequovendor.shared.generated.resources.order_net
import sequovendor.shared.generated.resources.order_packages
import sequovendor.shared.generated.resources.order_status_accepted
import sequovendor.shared.generated.resources.order_status_cancelled
import sequovendor.shared.generated.resources.order_status_handed_off
import sequovendor.shared.generated.resources.order_status_packed
import sequovendor.shared.generated.resources.order_status_pending
import sequovendor.shared.generated.resources.order_status_preparing
import sequovendor.shared.generated.resources.order_status_rejected

internal enum class DashboardConnectionState {
    Connected,
    LocalFallback,
    ErrorFallback,
}

@Composable
internal fun DashboardContent(
    session: VendorSession,
    orders: List<MerchantSubOrder>,
    products: List<CatalogProduct>,
    store: StoreAvailabilityState,
    connectionState: DashboardConnectionState,
    onStoreOpenChange: (Boolean) -> Unit,
    onOrdersClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pending = orders.count { it.status == MerchantSubOrderStatus.MERCHANT_PENDING }
    val accepted = orders.count { it.status == MerchantSubOrderStatus.ACCEPTED }
    val preparing = orders.count { it.status == MerchantSubOrderStatus.PREPARING }
    val ready = orders.count { it.status == MerchantSubOrderStatus.PACKED_READY }
    val todayNet = orders
        .filter { it.status != MerchantSubOrderStatus.REJECTED && it.status != MerchantSubOrderStatus.CANCELLED }
        .sumOf { it.merchantNetCfa }
    val productsNeedingPhotos = products.count { !it.cameraVerified }
    val responseWarnings = orders
        .filter { it.status == MerchantSubOrderStatus.MERCHANT_PENDING && !it.sellerResponseDueAt.isNullOrBlank() }
        .take(2)
    val packingWarnings = orders
        .filter {
            it.status == MerchantSubOrderStatus.ACCEPTED ||
                it.status == MerchantSubOrderStatus.PREPARING ||
                it.status == MerchantSubOrderStatus.PACKED_READY
        }
        .filter { !it.packingDueAt.isNullOrBlank() }
        .take(2)
    val recentOrders = orders
        .filter { it.status != MerchantSubOrderStatus.CANCELLED && it.status != MerchantSubOrderStatus.REJECTED }
        .take(4)

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
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(appText(Res.string.dashboard_store_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    KeyValueRow(
                        label = if (store.open) appText(Res.string.home_store_online) else appText(Res.string.home_store_offline),
                        value = store.openStatus ?: if (store.open) "OPEN" else "PAUSED",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onStoreOpenChange(!store.open) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (store.open) appText(Res.string.dashboard_close_store) else appText(Res.string.dashboard_open_store))
                        }
                        OutlinedButton(onClick = onStoreClick, modifier = Modifier.weight(1f)) {
                            Text(appText(Res.string.nav_store))
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(appText(Res.string.home_pending), pending.toString(), Modifier.weight(1f))
                MetricTile(appText(Res.string.dashboard_accepted), accepted.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(appText(Res.string.order_status_preparing), preparing.toString(), Modifier.weight(1f))
                MetricTile(appText(Res.string.home_ready), ready.toString(), Modifier.weight(1f))
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
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.dashboard_sla_warnings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (responseWarnings.isEmpty() && packingWarnings.isEmpty()) {
                        Text(appText(Res.string.dashboard_no_sla_warnings), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    responseWarnings.forEach { order ->
                        DashboardSlaRow(appText(Res.string.dashboard_response_due), order, order.sellerResponseDueAt)
                    }
                    packingWarnings.forEach { order ->
                        DashboardSlaRow(appText(Res.string.dashboard_packing_due), order, order.packingDueAt)
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.dashboard_quick_actions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardActionRow(Icons.Filled.ReceiptLong, appText(Res.string.dashboard_view_pending), "${appText(Res.string.order_status_pending)}: $pending", onOrdersClick)
                    DashboardActionRow(Icons.Filled.Inventory2, appText(Res.string.dashboard_edit_catalog), productsNeedingPhotos.toString(), onCatalogClick)
                    DashboardActionRow(Icons.Filled.Storefront, if (store.open) appText(Res.string.dashboard_close_store) else appText(Res.string.dashboard_open_store), store.weekdayHours) {
                        onStoreOpenChange(!store.open)
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.dashboard_recent_orders), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (recentOrders.isEmpty()) {
                        Text(appText(Res.string.dashboard_no_recent_orders), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        recentOrders.forEach { order ->
                            DashboardOrderRow(order)
                        }
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(appText(Res.string.dashboard_api_connection), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(connectionState.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DashboardSlaRow(
    label: String,
    order: MerchantSubOrder,
    dueAt: String?,
) {
    KeyValueRow("$label ${order.subOrderCode}", dueAt.formatIsoTimestampOrDash())
}

@Composable
private fun DashboardOrderRow(order: MerchantSubOrder) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(order.subOrderCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(order.status.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${appText(Res.string.order_net)}: ${order.merchantNetCfa.formatCfa()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${appText(Res.string.order_packages)}: ${order.packageCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            order.updatedAt?.let {
                Text("${appText(Res.string.dashboard_last_updated)}: ${it.formatIsoTimestampOrDash()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DashboardConnectionState.label(): String = when (this) {
    DashboardConnectionState.Connected -> appText(Res.string.dashboard_api_connected)
    DashboardConnectionState.LocalFallback -> appText(Res.string.dashboard_api_local)
    DashboardConnectionState.ErrorFallback -> appText(Res.string.dashboard_api_error)
}

@Composable
private fun MerchantSubOrderStatus.label(): String = when (this) {
    MerchantSubOrderStatus.MERCHANT_PENDING -> appText(Res.string.order_status_pending)
    MerchantSubOrderStatus.ACCEPTED -> appText(Res.string.order_status_accepted)
    MerchantSubOrderStatus.PREPARING -> appText(Res.string.order_status_preparing)
    MerchantSubOrderStatus.PACKED_READY -> appText(Res.string.order_status_packed)
    MerchantSubOrderStatus.HANDED_TO_COURIER -> appText(Res.string.order_status_handed_off)
    MerchantSubOrderStatus.REJECTED -> appText(Res.string.order_status_rejected)
    MerchantSubOrderStatus.CANCELLED -> appText(Res.string.order_status_cancelled)
}
