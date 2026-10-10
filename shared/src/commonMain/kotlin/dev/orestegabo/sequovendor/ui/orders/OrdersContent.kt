package dev.orestegabo.sequovendor.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.VendorOrderHandoffMethod
import dev.orestegabo.sequovendor.model.formatCfa
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorErrorKind
import dev.orestegabo.sequovendor.ui.components.VendorErrorPanel
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.order_accept
import sequovendor.shared.generated.resources.order_commission
import sequovendor.shared.generated.resources.order_created_at
import sequovendor.shared.generated.resources.order_handoff
import sequovendor.shared.generated.resources.order_item_subtotal
import sequovendor.shared.generated.resources.order_mark_packed
import sequovendor.shared.generated.resources.order_net
import sequovendor.shared.generated.resources.order_packages
import sequovendor.shared.generated.resources.order_packing_due
import sequovendor.shared.generated.resources.order_reject
import sequovendor.shared.generated.resources.order_rejection_reason
import sequovendor.shared.generated.resources.order_rejection_reason_placeholder
import sequovendor.shared.generated.resources.order_rejection_reason_required
import sequovendor.shared.generated.resources.order_rejection_saved_local
import sequovendor.shared.generated.resources.order_seller_response_due
import sequovendor.shared.generated.resources.order_start_preparation
import sequovendor.shared.generated.resources.order_status_accepted
import sequovendor.shared.generated.resources.order_status_cancelled
import sequovendor.shared.generated.resources.order_status_handed_off
import sequovendor.shared.generated.resources.order_status_packed
import sequovendor.shared.generated.resources.order_status_pending
import sequovendor.shared.generated.resources.order_status_preparing
import sequovendor.shared.generated.resources.order_status_rejected
import sequovendor.shared.generated.resources.order_submit_rejection
import sequovendor.shared.generated.resources.orders_action_failed
import sequovendor.shared.generated.resources.orders_detail
import sequovendor.shared.generated.resources.orders_empty
import sequovendor.shared.generated.resources.orders_handoff
import sequovendor.shared.generated.resources.orders_handoff_customer
import sequovendor.shared.generated.resources.orders_handoff_independent_rider
import sequovendor.shared.generated.resources.orders_handoff_relay
import sequovendor.shared.generated.resources.orders_handoff_sequo_rider
import sequovendor.shared.generated.resources.orders_loading
import sequovendor.shared.generated.resources.orders_refresh
import sequovendor.shared.generated.resources.orders_tab_accepted
import sequovendor.shared.generated.resources.orders_tab_completed
import sequovendor.shared.generated.resources.orders_tab_new
import sequovendor.shared.generated.resources.orders_tab_preparing
import sequovendor.shared.generated.resources.orders_tab_ready
import sequovendor.shared.generated.resources.orders_tab_rejected_cancelled
import sequovendor.shared.generated.resources.orders_title

internal enum class VendorOrderAction {
    Accept,
    StartPreparation,
    MarkPacked,
    Handoff,
    Reject,
}

@Composable
internal fun OrdersContent(
    orders: List<MerchantSubOrder>,
    isLoading: Boolean,
    isOffline: Boolean,
    actionInProgressOrderId: String?,
    actionFailed: Boolean,
    onRefresh: () -> Unit,
    onOrderAction: (MerchantSubOrder, VendorOrderAction, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(VendorOrdersTab.New) }
    val tabs = VendorOrdersTab.entries
    val visibleOrders = orders.filter { selectedTab.accepts(it) }

    LazyColumn(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(appText(Res.string.orders_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(appText(Res.string.orders_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = onRefresh, enabled = !isLoading) {
                        Text(appText(Res.string.orders_refresh))
                    }
                }
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
        if (isOffline) {
            item {
                VendorErrorPanel(VendorErrorKind.Offline, onRetry = onRefresh)
            }
        }
        if (actionFailed) {
            item {
                VendorCard {
                    Text(
                        appText(Res.string.orders_action_failed),
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        if (isLoading && orders.isEmpty()) {
            item {
                VendorCard {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Text(appText(Res.string.orders_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else if (visibleOrders.isEmpty()) {
            item {
                VendorCard {
                    Text(appText(Res.string.orders_empty), modifier = Modifier.padding(18.dp))
                }
            }
        }
        items(visibleOrders, key = { it.id }) { order ->
            OrderCard(
                order = order,
                actionInProgress = actionInProgressOrderId == order.id,
                onOrderAction = onOrderAction,
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: MerchantSubOrder,
    actionInProgress: Boolean,
    onOrderAction: (MerchantSubOrder, VendorOrderAction, String?) -> Unit,
) {
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(order.subOrderCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(order.status.label(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            KeyValueRow(appText(Res.string.order_item_subtotal), order.itemSubtotalCfa.formatCfa())
            KeyValueRow(appText(Res.string.order_net), order.merchantNetCfa.formatCfa())
            if (order.commissionCfa > 0) {
                KeyValueRow(appText(Res.string.order_commission), order.commissionCfa.formatCfa())
            }
            KeyValueRow(appText(Res.string.order_created_at), order.createdAt.formatIsoTimestampOrDash())
            order.sellerResponseDueAt?.let {
                KeyValueRow(appText(Res.string.order_seller_response_due), it.formatIsoTimestampOrDash())
            }
            order.packingDueAt?.let {
                KeyValueRow(appText(Res.string.order_packing_due), it.formatIsoTimestampOrDash())
            }
            KeyValueRow(appText(Res.string.order_packages), order.packageCount.toString())
            if (order.status == MerchantSubOrderStatus.HANDED_TO_COURIER) {
                KeyValueRow(appText(Res.string.orders_handoff), order.handoffMethod.label(order.handoffNote))
            }
            if (order.status == MerchantSubOrderStatus.REJECTED && !order.rejectionReason.isNullOrBlank()) {
                KeyValueRow(appText(Res.string.order_rejection_reason), order.rejectionReason)
            }

            OrderPrimaryActions(order, actionInProgress, onOrderAction)

            if (canReject) {
                if (!rejectBoxOpen) {
                    OutlinedButton(
                        onClick = { rejectBoxOpen = true },
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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
                                },
                            )
                        },
                        isError = rejectionReason.isBlank() && !rejectionSubmitted,
                        enabled = !actionInProgress,
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
                            onOrderAction(order, VendorOrderAction.Reject, rejectionReason)
                        },
                        enabled = rejectionReason.isNotBlank() && !actionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(appText(Res.string.order_submit_rejection))
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderPrimaryActions(
    order: MerchantSubOrder,
    actionInProgress: Boolean,
    onOrderAction: (MerchantSubOrder, VendorOrderAction, String?) -> Unit,
) {
    val action = when (order.status) {
        MerchantSubOrderStatus.MERCHANT_PENDING -> VendorOrderAction.Accept to appText(Res.string.order_accept)
        MerchantSubOrderStatus.ACCEPTED -> VendorOrderAction.StartPreparation to appText(Res.string.order_start_preparation)
        MerchantSubOrderStatus.PREPARING -> VendorOrderAction.MarkPacked to appText(Res.string.order_mark_packed)
        MerchantSubOrderStatus.PACKED_READY -> VendorOrderAction.Handoff to appText(Res.string.order_handoff)
        MerchantSubOrderStatus.HANDED_TO_COURIER,
        MerchantSubOrderStatus.REJECTED,
        MerchantSubOrderStatus.CANCELLED,
        -> null
    } ?: return

    Button(
        onClick = { onOrderAction(order, action.first, null) },
        enabled = !actionInProgress,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(action.second)
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
    New,
    Accepted,
    Preparing,
    Ready,
    Completed,
    RejectedCancelled,
}

@Composable
private fun VendorOrdersTab.label(): String = when (this) {
    VendorOrdersTab.New -> appText(Res.string.orders_tab_new)
    VendorOrdersTab.Accepted -> appText(Res.string.orders_tab_accepted)
    VendorOrdersTab.Preparing -> appText(Res.string.orders_tab_preparing)
    VendorOrdersTab.Ready -> appText(Res.string.orders_tab_ready)
    VendorOrdersTab.Completed -> appText(Res.string.orders_tab_completed)
    VendorOrdersTab.RejectedCancelled -> appText(Res.string.orders_tab_rejected_cancelled)
}

private fun VendorOrdersTab.accepts(order: MerchantSubOrder): Boolean =
    when (this) {
        VendorOrdersTab.New -> order.status == MerchantSubOrderStatus.MERCHANT_PENDING
        VendorOrdersTab.Accepted -> order.status == MerchantSubOrderStatus.ACCEPTED
        VendorOrdersTab.Preparing -> order.status == MerchantSubOrderStatus.PREPARING
        VendorOrdersTab.Ready -> order.status == MerchantSubOrderStatus.PACKED_READY
        VendorOrdersTab.Completed -> order.status == MerchantSubOrderStatus.HANDED_TO_COURIER
        VendorOrdersTab.RejectedCancelled -> order.status in setOf(
            MerchantSubOrderStatus.REJECTED,
            MerchantSubOrderStatus.CANCELLED,
        )
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
