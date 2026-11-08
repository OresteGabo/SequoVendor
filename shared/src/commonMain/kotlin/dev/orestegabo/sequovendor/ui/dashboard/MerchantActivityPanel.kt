package dev.orestegabo.sequovendor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.activity.MerchantActivitySnapshot
import dev.orestegabo.sequovendor.core.activity.MerchantPayoutSnapshot
import dev.orestegabo.sequovendor.core.activity.MerchantSettlementStage
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.formatCfa
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.*

@Composable
internal fun MerchantActivityPanel(
    snapshot: MerchantActivitySnapshot?,
    loading: Boolean,
    failure: ApiFailure?,
    localMode: Boolean,
    onRefresh: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val actual = snapshot.takeUnless { loading || failure != null || localMode }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(appText(Res.string.finance_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(appText(Res.string.finance_scope), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onRefresh, enabled = !loading && !localMode) { Text(appText(Res.string.finance_refresh)) }
        when {
            localMode -> Text(appText(Res.string.finance_local))
            loading -> Text(appText(Res.string.finance_loading))
            failure != null -> {
                Text(appText(failure.userMessage), color = MaterialTheme.colorScheme.error)
                Text(appText(Res.string.finance_hidden))
            }
        }
        if (actual == null) return@Column
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            listOf(Res.string.finance_sales, Res.string.finance_returns, Res.string.finance_payouts).forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(appText(label)) })
            }
        }
        when (selectedTab) {
            0 -> {
                if (actual.orders.isEmpty()) Text(appText(Res.string.finance_empty_sales))
                actual.orders.forEach { order ->
                    ActivityRecord("${order.subOrderCode} · ${order.status.label()}") {
                        ActivityField(appText(Res.string.order_id), order.orderId ?: unknown())
                        ActivityField(appText(Res.string.finance_order_state), merchantOrderState(order.orderStatus))
                        ActivityField(appText(Res.string.finance_customer_payment), customerPaymentState(order.orderPaymentStatus))
                        ActivityField(appText(Res.string.finance_created), order.orderCreatedAt.formatIsoTimestampOrDash())
                        order.deliveredAt?.let { ActivityField(appText(Res.string.finance_delivered), it.formatIsoTimestampOrDash()) }
                        order.lines.forEach { Text("${it.quantity} × ${it.productName}") }
                        ActivityField(appText(Res.string.order_item_subtotal), order.itemSubtotalCfa.formatCfa())
                        ActivityField(appText(Res.string.order_commission), order.commissionCfa.formatCfa())
                        ActivityField(appText(Res.string.finance_order_net), order.merchantNetCfa.formatCfa())
                    }
                }
            }
            1 -> {
                if (actual.returns.isEmpty()) Text(appText(Res.string.finance_empty_returns))
                actual.returns.forEach { item ->
                    ActivityRecord(item.id) {
                        ActivityField(appText(Res.string.order_id), item.orderId)
                        ActivityField(appText(Res.string.account_status), returnState(item.status))
                        item.lines.forEach { Text("${it.quantity} × ${it.productName}") }
                        ActivityField(appText(Res.string.finance_return_requested_at), item.requestedAt.formatIsoTimestampOrDash())
                        item.droppedAt?.let { ActivityField(appText(Res.string.finance_return_dropped_at), it.formatIsoTimestampOrDash()) }
                        item.collectedBySequoAt?.let { ActivityField(appText(Res.string.finance_return_collected_at), it.formatIsoTimestampOrDash()) }
                        item.receivedBySequoAt?.let { ActivityField(appText(Res.string.finance_return_received_at), it.formatIsoTimestampOrDash()) }
                        ActivityField(appText(Res.string.finance_inspection), when (item.inspectionDecision) {
                            "ACCEPTED" -> appText(Res.string.finance_inspection_accepted)
                            "REJECTED" -> appText(Res.string.finance_inspection_rejected)
                            else -> unknown()
                        })
                        ActivityField(appText(Res.string.finance_customer_payment), customerPaymentState(item.orderPaymentStatus))
                        ActivityField(appText(Res.string.finance_refund_instruction), when (item.refundInstructionState) {
                            "REQUESTED" -> appText(Res.string.finance_refund_requested)
                            "NOT_INSTRUCTED" -> appText(Res.string.finance_refund_not_instructed)
                            "LEGACY_REVIEW_REQUIRED" -> appText(Res.string.finance_legacy_review)
                            else -> unknown()
                        })
                        ActivityField(appText(Res.string.finance_refund_payment), when (item.refundPaymentStatus) {
                            "NOT_SUBMITTED" -> appText(Res.string.finance_refund_not_submitted)
                            "UNCONFIRMED" -> appText(Res.string.finance_refund_unconfirmed)
                            else -> unknown()
                        })
                    }
                }
            }
            2 -> {
                Text(appText(Res.string.finance_no_transfer_confirmation), style = MaterialTheme.typography.bodySmall)
                if (actual.payouts.isEmpty()) Text(appText(Res.string.finance_empty_payouts))
                actual.payouts.forEach { payout ->
                    ActivityRecord(payout.id) {
                        ActivityField(appText(Res.string.order_id), payout.orderId)
                        ActivityField(appText(Res.string.finance_source_line), payout.sourceOrderItemId)
                        ActivityField(appText(Res.string.account_status), payoutState(payout))
                        ActivityField(appText(Res.string.finance_payout_net), payout.merchantNetCfa.formatCfa())
                        ActivityField(appText(Res.string.order_commission), payout.commissionCfa.formatCfa())
                        ActivityField(appText(Res.string.finance_received), payout.packageReceivedAt.formatIsoTimestampOrDash())
                        ActivityField(appText(Res.string.finance_eligible), payout.payoutEligibleAt.formatIsoTimestampOrDash())
                        ActivityField(appText(Res.string.finance_due), payout.payoutDueBy.formatIsoTimestampOrDash())
                        if (payout.activeReturnHold) Text(appText(Res.string.finance_return_hold))
                        if (payout.activeDisputeHold) Text(appText(Res.string.finance_dispute_hold))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRecord(title: String, content: @Composable () -> Unit) {
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
internal fun ActivityField(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun unknown() = appText(Res.string.finance_unknown)

@Composable
internal fun customerPaymentState(status: String?): String = when (status) {
    "Pending" -> appText(Res.string.finance_payment_pending)
    "Validated" -> appText(Res.string.finance_payment_validated)
    "Failed" -> appText(Res.string.finance_payment_failed)
    "Cancelled" -> appText(Res.string.finance_payment_cancelled)
    "BlockedByPolicy" -> appText(Res.string.finance_payment_blocked)
    else -> unknown()
}

@Composable
internal fun merchantOrderState(status: String?): String = when (status) {
    "ACCEPTED_FOR_FULFILLMENT" -> appText(Res.string.finance_order_accepted)
    "DELIVERED" -> appText(Res.string.finance_order_delivered)
    "CANCELLED" -> appText(Res.string.finance_order_cancelled)
    "RETURN_REQUESTED" -> appText(Res.string.finance_order_return_requested)
    "REFUNDED" -> appText(Res.string.finance_order_refunded)
    else -> unknown()
}

@Composable
private fun returnState(status: String): String = when (status) {
    "Requested" -> appText(Res.string.finance_return_requested)
    "AwaitingRelayDropoff" -> appText(Res.string.finance_return_awaiting_dropoff)
    "DroppedAtRelay" -> appText(Res.string.finance_return_dropped)
    "CollectedBySequo" -> appText(Res.string.finance_return_collected)
    "ReceivedBySequo" -> appText(Res.string.finance_return_received)
    "RefundPending" -> appText(Res.string.finance_return_refund_pending)
    "Rejected" -> appText(Res.string.finance_return_rejected)
    else -> unknown()
}

@Composable
private fun payoutState(payout: MerchantPayoutSnapshot): String {
    val label = when (payout.recordedStage) {
        MerchantSettlementStage.PENDING -> appText(Res.string.finance_payout_pending)
        MerchantSettlementStage.ISSUED -> appText(Res.string.finance_payout_issued)
        MerchantSettlementStage.SETTLED -> appText(Res.string.finance_payout_recorded_settled)
        MerchantSettlementStage.FAILED -> appText(Res.string.finance_payout_failed)
        MerchantSettlementStage.ADJUSTED -> appText(Res.string.finance_payout_adjusted)
        MerchantSettlementStage.UNKNOWN -> unknown()
    }
    return "$label (${payout.status})"
}
