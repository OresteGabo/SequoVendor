package dev.orestegabo.sequovendor.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.VendorOrderHandoffMethod
import dev.orestegabo.sequovendor.model.formatCfa
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.order_net
import sequovendor.shared.generated.resources.order_packages
import sequovendor.shared.generated.resources.order_reject
import sequovendor.shared.generated.resources.order_rejection_reason
import sequovendor.shared.generated.resources.order_rejection_reason_placeholder
import sequovendor.shared.generated.resources.order_rejection_reason_required
import sequovendor.shared.generated.resources.order_rejection_saved_local
import sequovendor.shared.generated.resources.order_sla
import sequovendor.shared.generated.resources.order_status_accepted
import sequovendor.shared.generated.resources.order_status_cancelled
import sequovendor.shared.generated.resources.order_status_handed_off
import sequovendor.shared.generated.resources.order_status_packed
import sequovendor.shared.generated.resources.order_status_pending
import sequovendor.shared.generated.resources.order_status_preparing
import sequovendor.shared.generated.resources.order_status_rejected
import sequovendor.shared.generated.resources.order_submit_rejection
import sequovendor.shared.generated.resources.orders_handoff
import sequovendor.shared.generated.resources.orders_handoff_customer
import sequovendor.shared.generated.resources.orders_handoff_independent_rider
import sequovendor.shared.generated.resources.orders_handoff_relay
import sequovendor.shared.generated.resources.orders_handoff_sequo_rider
import sequovendor.shared.generated.resources.orders_detail
import sequovendor.shared.generated.resources.orders_empty
import sequovendor.shared.generated.resources.orders_tab_active
import sequovendor.shared.generated.resources.orders_tab_handoff
import sequovendor.shared.generated.resources.orders_tab_refused
import sequovendor.shared.generated.resources.orders_title

@Composable
internal fun OrdersContent(
    orders: List<MerchantSubOrder>,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(VendorOrdersTab.Active) }
    val tabs = VendorOrdersTab.entries
    val visibleOrders = orders.filter { selectedTab.accepts(it) }

    LazyColumn(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.orders_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.orders_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            ScrollableTabRow(selectedTabIndex = selectedTab.ordinal, edgePadding = 0.dp) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text("${tab.label()} (${orders.count { tab.accepts(it) }})") },
                    )
                }
            }
        }
        if (visibleOrders.isEmpty()) {
            item {
                VendorCard {
                    Text(appText(Res.string.orders_empty), modifier = Modifier.padding(18.dp))
                }
            }
        }
        items(visibleOrders, key = { it.id }) { order ->
            OrderCard(order)
        }
    }
}

@Composable
private fun OrderCard(order: MerchantSubOrder) {
    var rejectBoxOpen by remember(order.id) { mutableStateOf(false) }
    var rejectionReason by remember(order.id) { mutableStateOf(order.rejectionReason.orEmpty()) }
    var rejectionSubmitted by remember(order.id) { mutableStateOf(order.rejectionReason?.isNotBlank() == true) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val canReject = order.status in setOf(
        MerchantSubOrderStatus.MERCHANT_PENDING,
        MerchantSubOrderStatus.ACCEPTED,
    )

    LaunchedEffect(rejectBoxOpen) {
        if (rejectBoxOpen) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(order.status.label(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            KeyValueRow(appText(Res.string.order_net), order.merchantNetCfa.formatCfa())
            KeyValueRow(appText(Res.string.order_packages), order.packageCount.toString())
            KeyValueRow(appText(Res.string.order_sla), (order.packingDueAt ?: order.sellerResponseDueAt).formatIsoTimestampOrDash())
            if (order.status == MerchantSubOrderStatus.HANDED_TO_COURIER) {
                KeyValueRow(appText(Res.string.orders_handoff), order.handoffMethod.label(order.handoffNote))
            }
            if (order.status == MerchantSubOrderStatus.REJECTED && !order.rejectionReason.isNullOrBlank()) {
                KeyValueRow(appText(Res.string.order_rejection_reason), order.rejectionReason)
            }
            if (canReject) {
                if (!rejectBoxOpen) {
                    OutlinedButton(onClick = { rejectBoxOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.order_reject))
                    }
                } else {
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = {
                            rejectionReason = sanitizeRejectionReason(it)
                            rejectionSubmitted = false
                        },
                        label = { Text(appText(Res.string.order_rejection_reason)) },
                        placeholder = { Text(appText(Res.string.order_rejection_reason_placeholder)) },
                        supportingText = {
                            Text(
                                if (rejectionSubmitted) {
                                    appText(Res.string.order_rejection_saved_local)
                                } else {
                                    appText(Res.string.order_rejection_reason_required)
                                        .replace("%1\$s", rejectionReason.length.toString())
                                }
                            )
                        },
                        isError = rejectionReason.isBlank() && !rejectionSubmitted,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                        ),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    )
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            rejectionSubmitted = true
                        },
                        enabled = rejectionReason.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(appText(Res.string.order_submit_rejection))
                    }
                }
            }
        }
    }
}

private fun sanitizeRejectionReason(input: String): String =
    input
        .filter { it.isAllowedRejectionReasonChar() }
        .take(MaxRejectionReasonLength)

private fun Char.isAllowedRejectionReasonChar(): Boolean =
    isLetterOrDigit() ||
        isWhitespace() ||
        this in setOf('.', ',', '\'', '"', '’', '-', '/', '!', '?', ':', ';', '(', ')')

private const val MaxRejectionReasonLength = 100

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

private enum class VendorOrdersTab {
    Active,
    Refused,
    Handoff,
}

@Composable
private fun VendorOrdersTab.label(): String = when (this) {
    VendorOrdersTab.Active -> appText(Res.string.orders_tab_active)
    VendorOrdersTab.Refused -> appText(Res.string.orders_tab_refused)
    VendorOrdersTab.Handoff -> appText(Res.string.orders_tab_handoff)
}

private fun VendorOrdersTab.accepts(order: MerchantSubOrder): Boolean =
    when (this) {
        VendorOrdersTab.Active -> order.status in setOf(
            MerchantSubOrderStatus.MERCHANT_PENDING,
            MerchantSubOrderStatus.ACCEPTED,
            MerchantSubOrderStatus.PREPARING,
            MerchantSubOrderStatus.PACKED_READY,
        )
        VendorOrdersTab.Refused -> order.status in setOf(
            MerchantSubOrderStatus.REJECTED,
            MerchantSubOrderStatus.CANCELLED,
        )
        VendorOrdersTab.Handoff -> order.status == MerchantSubOrderStatus.HANDED_TO_COURIER
    }

@Composable
private fun VendorOrderHandoffMethod?.label(fallback: String?): String =
    when (this) {
        VendorOrderHandoffMethod.SEQUO_RIDER -> appText(Res.string.orders_handoff_sequo_rider)
        VendorOrderHandoffMethod.INDEPENDENT_RIDER -> appText(Res.string.orders_handoff_independent_rider)
        VendorOrderHandoffMethod.CUSTOMER_PICKUP -> appText(Res.string.orders_handoff_customer)
        VendorOrderHandoffMethod.RELAY_POINT -> appText(Res.string.orders_handoff_relay)
        null -> fallback?.takeIf { it.isNotBlank() } ?: "-"
    }
