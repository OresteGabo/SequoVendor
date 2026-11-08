package dev.orestegabo.sequovendor.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.network.ApiHealthStatus
import dev.orestegabo.sequovendor.feature.legal.LegalScreen
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.model.VendorNegotiationStatus
import dev.orestegabo.sequovendor.model.VendorNegotiationThread
import dev.orestegabo.sequovendor.model.formatCfa
import dev.orestegabo.sequovendor.model.formatCfaOrDash
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorChip
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.account_api_check
import sequovendor.shared.generated.resources.account_api_health
import sequovendor.shared.generated.resources.account_api_healthy
import sequovendor.shared.generated.resources.account_api_unreachable
import sequovendor.shared.generated.resources.account_availability
import sequovendor.shared.generated.resources.account_demo
import sequovendor.shared.generated.resources.account_detail
import sequovendor.shared.generated.resources.account_email
import sequovendor.shared.generated.resources.account_exceptional_closure
import sequovendor.shared.generated.resources.account_language
import sequovendor.shared.generated.resources.account_legal_privacy
import sequovendor.shared.generated.resources.account_manage_store
import sequovendor.shared.generated.resources.account_merchant_context
import sequovendor.shared.generated.resources.account_merchant_id
import sequovendor.shared.generated.resources.account_notifications_entry
import dev.orestegabo.sequovendor.core.network.ApiFailure
import sequovendor.shared.generated.resources.account_negotiation_accepted_price
import sequovendor.shared.generated.resources.account_negotiation_customer
import sequovendor.shared.generated.resources.account_negotiation_merchant
import sequovendor.shared.generated.resources.account_negotiation_refresh
import sequovendor.shared.generated.resources.account_negotiation_loading
import sequovendor.shared.generated.resources.account_negotiation_reply
import sequovendor.shared.generated.resources.account_negotiation_send
import sequovendor.shared.generated.resources.account_negotiation_history
import sequovendor.shared.generated.resources.account_negotiation_live
import sequovendor.shared.generated.resources.account_negotiation_status_open
import sequovendor.shared.generated.resources.account_negotiation_status_locked
import sequovendor.shared.generated.resources.account_negotiation_status_cancelled
import sequovendor.shared.generated.resources.account_negotiation_status_unknown
import sequovendor.shared.generated.resources.account_negotiation_accept
import sequovendor.shared.generated.resources.account_negotiation_counter
import sequovendor.shared.generated.resources.account_negotiation_counter_placeholder
import sequovendor.shared.generated.resources.account_negotiation_customer_offer
import sequovendor.shared.generated.resources.account_negotiation_demo
import sequovendor.shared.generated.resources.account_negotiation_empty
import sequovendor.shared.generated.resources.account_negotiation_error
import sequovendor.shared.generated.resources.account_negotiation_lock_expiry
import sequovendor.shared.generated.resources.account_negotiation_product
import sequovendor.shared.generated.resources.account_negotiation_reason
import sequovendor.shared.generated.resources.account_negotiation_reject
import sequovendor.shared.generated.resources.account_negotiation_seller_counter
import sequovendor.shared.generated.resources.account_negotiation_status
import sequovendor.shared.generated.resources.account_negotiation_attempts
import sequovendor.shared.generated.resources.account_negotiation_status_accepted
import sequovendor.shared.generated.resources.account_negotiation_status_countered
import sequovendor.shared.generated.resources.account_negotiation_status_customer_offer
import sequovendor.shared.generated.resources.account_negotiation_status_expired
import sequovendor.shared.generated.resources.account_negotiation_status_rejected
import sequovendor.shared.generated.resources.account_negotiations
import sequovendor.shared.generated.resources.account_offline
import sequovendor.shared.generated.resources.account_open_notifications
import sequovendor.shared.generated.resources.account_online
import sequovendor.shared.generated.resources.account_payment_card
import sequovendor.shared.generated.resources.account_payment_cash
import sequovendor.shared.generated.resources.account_payment_mobile_money
import sequovendor.shared.generated.resources.account_payments_accepted
import sequovendor.shared.generated.resources.account_profile
import sequovendor.shared.generated.resources.account_provider
import sequovendor.shared.generated.resources.account_sales_payments
import sequovendor.shared.generated.resources.account_status
import sequovendor.shared.generated.resources.account_theme
import sequovendor.shared.generated.resources.account_title
import sequovendor.shared.generated.resources.admin_login_title
import sequovendor.shared.generated.resources.admin_login_warning
import sequovendor.shared.generated.resources.common_not_available_yet
import sequovendor.shared.generated.resources.dashboard_merchant_net_pending
import sequovendor.shared.generated.resources.dashboard_returns_awaiting
import sequovendor.shared.generated.resources.dashboard_sales_today
import sequovendor.shared.generated.resources.dashboard_settlement_state
import sequovendor.shared.generated.resources.drawer_theme_dark
import sequovendor.shared.generated.resources.drawer_theme_light
import sequovendor.shared.generated.resources.drawer_theme_system
import sequovendor.shared.generated.resources.language_en
import sequovendor.shared.generated.resources.language_fr
import sequovendor.shared.generated.resources.shell_sign_out
import sequovendor.shared.generated.resources.store_weekday
import sequovendor.shared.generated.resources.store_weekend

@Composable
internal fun AccountContent(
    session: VendorSession,
    availability: StoreAvailabilityState,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    apiHealthStatus: ApiHealthStatus?,
    onCheckApiHealth: () -> Unit,
    onAvailabilityChange: (Boolean) -> Unit,
    onStoreClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    negotiations: List<VendorNegotiationThread>,
    negotiationActionInProgressId: String?,
    negotiationFailure: ApiFailure?,
    negotiationsLoading: Boolean,
    negotiationsStale: Boolean,
    onRefreshNegotiations: () -> Unit,
    onAcceptNegotiation: (VendorNegotiationThread) -> Unit,
    onRejectNegotiation: (VendorNegotiationThread, String) -> Unit,
    onCounterNegotiation: (VendorNegotiationThread, Int) -> Unit,
    onMessageNegotiation: (VendorNegotiationThread, String) -> Unit,
    salesToday: Int,
    merchantNetPendingCfa: Int,
    onLogout: () -> Unit,
    pendingAdminLoginRequestCount: Int = 0,
    onAdminLoginRequestsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.account_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.account_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            VendorCard {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(appText(Res.string.account_profile), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(session.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        session.email?.takeIf { it.isNotBlank() }?.let {
                            Text("${appText(Res.string.account_email)}: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        session.status?.takeIf { it.isNotBlank() }?.let {
                            Text("${appText(Res.string.account_status)}: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            if (session.localMode) {
                                appText(Res.string.account_demo)
                            } else {
                                "${appText(Res.string.account_provider)}: ${session.authProvider.providerLabel()}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = appText(Res.string.shell_sign_out), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(appText(Res.string.account_payments_accepted), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    PaymentChip(appText(Res.string.account_payment_mobile_money))
                    PaymentChip(appText(Res.string.account_payment_card))
                    PaymentChip(appText(Res.string.account_payment_cash))
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(appText(Res.string.account_sales_payments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    KeyValueRow(appText(Res.string.dashboard_sales_today), salesToday.toString())
                    KeyValueRow(appText(Res.string.dashboard_merchant_net_pending), merchantNetPendingCfa.formatCfa())
                    KeyValueRow(appText(Res.string.dashboard_returns_awaiting), appText(Res.string.common_not_available_yet))
                    KeyValueRow(appText(Res.string.dashboard_settlement_state), appText(Res.string.common_not_available_yet))
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(appText(Res.string.account_merchant_context), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    KeyValueRow(appText(Res.string.account_merchant_id), session.merchantId)
                    KeyValueRow(appText(Res.string.account_api_health), apiHealthStatus.healthLabel())
                    OutlinedButton(onClick = onCheckApiHealth, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.account_api_check))
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(appText(Res.string.account_language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VendorChip(appText(Res.string.language_en), language == AppLanguage.English, { onLanguageChange(AppLanguage.English) }, Modifier.weight(1f))
                        VendorChip(appText(Res.string.language_fr), language == AppLanguage.French, { onLanguageChange(AppLanguage.French) }, Modifier.weight(1f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(appText(Res.string.account_theme), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VendorChip(appText(Res.string.drawer_theme_system), themePreference == AppThemePreference.System, { onThemePreferenceChange(AppThemePreference.System) }, Modifier.weight(1f))
                        VendorChip(appText(Res.string.drawer_theme_light), themePreference == AppThemePreference.Light, { onThemePreferenceChange(AppThemePreference.Light) }, Modifier.weight(1f))
                        VendorChip(appText(Res.string.drawer_theme_dark), themePreference == AppThemePreference.Dark, { onThemePreferenceChange(AppThemePreference.Dark) }, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.account_notifications_entry), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onNotificationsClick, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.account_open_notifications))
                    }
                }
            }
        }

        if (pendingAdminLoginRequestCount > 0) {
            item {
                VendorCard {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(appText(Res.string.admin_login_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(appText(Res.string.admin_login_warning), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = onAdminLoginRequestsClick, modifier = Modifier.fillMaxWidth()) {
                            Text("${appText(Res.string.admin_login_title)} ($pendingAdminLoginRequestCount)")
                        }
                    }
                }
            }
        }

        item {
            NegotiationsCard(
                negotiations = negotiations,
                actionInProgressId = negotiationActionInProgressId,
                failure = negotiationFailure,
                loading = negotiationsLoading,
                stale = negotiationsStale,
                localMode = session.localMode,
                onRefresh = onRefreshNegotiations,
                onMessage = onMessageNegotiation,
                onAccept = onAcceptNegotiation,
                onReject = onRejectNegotiation,
                onCounter = onCounterNegotiation,
            )
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(appText(Res.string.account_availability), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    if (availability.open) appText(Res.string.account_online) else appText(Res.string.account_offline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Switch(checked = availability.open, onCheckedChange = onAvailabilityChange)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    KeyValueRow(appText(Res.string.store_weekday), availability.weekdayHours)
                    KeyValueRow(appText(Res.string.store_weekend), availability.weekendHours)
                    availability.temporaryClosure?.reason?.takeIf { it.isNotBlank() }?.let {
                        KeyValueRow(appText(Res.string.account_exceptional_closure), it)
                    }
                    OutlinedButton(onClick = onStoreClick, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.account_manage_store))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(appText(Res.string.account_legal_privacy), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LegalScreen(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NegotiationsCard(
    negotiations: List<VendorNegotiationThread>,
    actionInProgressId: String?,
    failure: ApiFailure?,
    loading: Boolean,
    stale: Boolean,
    localMode: Boolean,
    onRefresh: () -> Unit,
    onMessage: (VendorNegotiationThread, String) -> Unit,
    onAccept: (VendorNegotiationThread) -> Unit,
    onReject: (VendorNegotiationThread, String) -> Unit,
    onCounter: (VendorNegotiationThread, Int) -> Unit,
) {
    val rejectReasons = remember { mutableStateMapOf<String, String>() }
    val counterAmounts = remember { mutableStateMapOf<String, String>() }
    val replyMessages = remember { mutableStateMapOf<String, String>() }

    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(appText(Res.string.account_negotiations), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(appText(if (localMode) Res.string.account_negotiation_demo else Res.string.account_negotiation_live), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onRefresh, enabled = !loading && actionInProgressId == null) { Text(appText(Res.string.account_negotiation_refresh)) }
            if (loading) Text(appText(Res.string.account_negotiation_loading))
            if (failure != null) {
                Text(appText(failure.userMessage), color = MaterialTheme.colorScheme.error)
                Text(appText(Res.string.account_negotiation_error), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (!loading && failure == null && negotiations.isEmpty()) {
                Text(appText(Res.string.account_negotiation_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            negotiations.forEach { thread ->
                var expanded by remember(thread.id) { mutableStateOf(false) }
                val busy = loading || stale || actionInProgressId != null
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    KeyValueRow(appText(Res.string.account_negotiation_product), thread.productName)
                    KeyValueRow(appText(Res.string.account_negotiation_customer_offer), thread.customerOfferCfa.formatCfa())
                    KeyValueRow(appText(Res.string.account_negotiation_seller_counter), thread.sellerCounterCfa.formatCfaOrDash())
                    KeyValueRow(appText(Res.string.account_negotiation_attempts), "${thread.merchantAttemptsRemaining}/${thread.maxMerchantAttempts}")
                    thread.acceptedPriceCfa?.let { KeyValueRow(appText(Res.string.account_negotiation_accepted_price), it.formatCfa()) }
                    thread.lockExpiresAt?.let {
                        KeyValueRow(appText(Res.string.account_negotiation_lock_expiry), it.formatIsoTimestampOrDash())
                    }
                    KeyValueRow(appText(Res.string.account_negotiation_status), thread.status.label())
                    OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.account_negotiations))
                    }
                    if (expanded) {
                        Text(appText(Res.string.account_negotiation_history), fontWeight = FontWeight.Bold)
                        thread.messages.forEach { entry ->
                            val actor = when (entry.actorType) {
                                "CUSTOMER" -> appText(Res.string.account_negotiation_customer)
                                "MERCHANT" -> appText(Res.string.account_negotiation_merchant)
                                else -> "SEQUO"
                            }
                            val action = appText(when (entry.offerType) {
                                "OFFER" -> Res.string.account_negotiation_customer_offer
                                "COUNTER_OFFER" -> Res.string.account_negotiation_seller_counter
                                "ACCEPT" -> Res.string.account_negotiation_status_accepted
                                "REJECT" -> Res.string.account_negotiation_status_rejected
                                else -> Res.string.account_negotiation_reply
                            })
                            Text("$actor · $action · ${entry.createdAt.formatIsoTimestampOrDash()}", style = MaterialTheme.typography.bodySmall)
                            entry.amountCfa?.let { Text(it.formatCfa()) }
                            entry.message?.let { Text(it) }
                        }
                        OutlinedTextField(
                            value = replyMessages[thread.id].orEmpty(),
                            onValueChange = { replyMessages[thread.id] = it.take(500) },
                            label = { Text(appText(Res.string.account_negotiation_reply)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedButton(
                            onClick = { onMessage(thread, replyMessages[thread.id].orEmpty()) },
                            enabled = !busy && thread.status in setOf(VendorNegotiationStatus.CUSTOMER_OFFERED, VendorNegotiationStatus.MERCHANT_COUNTERED, VendorNegotiationStatus.OPEN, VendorNegotiationStatus.REJECTED) && replyMessages[thread.id].orEmpty().isNotBlank(),
                        ) { Text(appText(Res.string.account_negotiation_send)) }
                        OutlinedTextField(
                            value = counterAmounts[thread.id].orEmpty(),
                            onValueChange = { counterAmounts[thread.id] = it.filter(Char::isDigit).take(9) },
                            label = { Text(appText(Res.string.account_negotiation_counter_placeholder)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = rejectReasons[thread.id].orEmpty(),
                            onValueChange = { rejectReasons[thread.id] = sanitizeReason(it) },
                            label = { Text(appText(Res.string.account_negotiation_reason)) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAccept(thread) },
                                enabled = !busy && (thread.merchantActionAllowed && thread.status.allowsMerchantAction),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(appText(Res.string.account_negotiation_accept))
                            }
                            OutlinedButton(
                                onClick = { counterAmounts[thread.id]?.toIntOrNull()?.let { onCounter(thread, it) } },
                                enabled = !busy && (thread.merchantActionAllowed && thread.status.allowsMerchantAction) && (counterAmounts[thread.id]?.toIntOrNull() ?: 0) > 0 && thread.merchantAttemptsRemaining > 0,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(appText(Res.string.account_negotiation_counter))
                            }
                        }
                        OutlinedButton(
                            onClick = { onReject(thread, rejectReasons[thread.id].orEmpty()) },
                            enabled = !busy && (thread.merchantActionAllowed && thread.status.allowsMerchantAction) && rejectReasons[thread.id].orEmpty().isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(appText(Res.string.account_negotiation_reject))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) })
}

@Composable
private fun VendorNegotiationStatus.label(): String =
    when (this) {
        VendorNegotiationStatus.CUSTOMER_OFFERED -> appText(Res.string.account_negotiation_status_customer_offer)
        VendorNegotiationStatus.MERCHANT_COUNTERED -> appText(Res.string.account_negotiation_status_countered)
        VendorNegotiationStatus.ACCEPTED -> appText(Res.string.account_negotiation_status_accepted)
        VendorNegotiationStatus.REJECTED -> appText(Res.string.account_negotiation_status_rejected)
        VendorNegotiationStatus.EXPIRED -> appText(Res.string.account_negotiation_status_expired)
        VendorNegotiationStatus.OPEN -> appText(Res.string.account_negotiation_status_open)
        VendorNegotiationStatus.LOCKED -> appText(Res.string.account_negotiation_status_locked)
        VendorNegotiationStatus.CANCELLED -> appText(Res.string.account_negotiation_status_cancelled)
        VendorNegotiationStatus.UNKNOWN -> appText(Res.string.account_negotiation_status_unknown)
    }

private val VendorNegotiationStatus.allowsMerchantAction: Boolean
    get() = this == VendorNegotiationStatus.CUSTOMER_OFFERED

private fun sanitizeReason(input: String): String =
    input.filter { it.isLetterOrDigit() || it.isWhitespace() || it in "-_.'/,&()" }.take(120)

private fun String?.providerLabel(): String =
    when (this?.uppercase()) {
        "GOOGLE" -> "Google"
        "APPLE" -> "iCloud"
        "FACEBOOK" -> "Facebook"
        "EMAIL" -> "Email"
        "PASSKEY" -> "Passkey"
        else -> "Sequo"
    }

@Composable
private fun ApiHealthStatus?.healthLabel(): String =
    if (this?.healthy == true) {
        val status = actuatorStatus?.takeIf { it.isNotBlank() } ?: "UP"
        "${appText(Res.string.account_api_healthy)} ($status)"
    } else {
        appText(Res.string.account_api_unreachable)
    }
