package dev.orestegabo.sequovendor.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.account_availability
import sequovendor.shared.generated.resources.account_demo
import sequovendor.shared.generated.resources.account_detail
import sequovendor.shared.generated.resources.account_exceptional_closure
import sequovendor.shared.generated.resources.account_exceptional_closure_placeholder
import sequovendor.shared.generated.resources.account_local_notice
import sequovendor.shared.generated.resources.account_offline
import sequovendor.shared.generated.resources.account_online
import sequovendor.shared.generated.resources.account_payment_card
import sequovendor.shared.generated.resources.account_payment_cash
import sequovendor.shared.generated.resources.account_payment_mobile_money
import sequovendor.shared.generated.resources.account_payments_accepted
import sequovendor.shared.generated.resources.account_profile
import sequovendor.shared.generated.resources.account_provider
import sequovendor.shared.generated.resources.account_signed_in
import sequovendor.shared.generated.resources.account_title
import sequovendor.shared.generated.resources.shell_sign_out
import sequovendor.shared.generated.resources.store_weekday
import sequovendor.shared.generated.resources.store_weekend

@Composable
internal fun AccountContent(
    session: VendorSession,
    availability: StoreAvailabilityState,
    closureNote: String,
    onAvailabilityChange: (Boolean) -> Unit,
    onClosureNoteChange: (String) -> Unit,
    onLogout: () -> Unit,
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
                    OutlinedTextField(
                        value = closureNote,
                        onValueChange = onClosureNoteChange,
                        label = { Text(appText(Res.string.account_exceptional_closure)) },
                        placeholder = { Text(appText(Res.string.account_exceptional_closure_placeholder)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 92.dp),
                        minLines = 2,
                    )
                    Text(appText(Res.string.account_local_notice), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PaymentChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) })
}

private fun String?.providerLabel(): String =
    when (this?.uppercase()) {
        "GOOGLE" -> "Google"
        "APPLE" -> "iCloud"
        "FACEBOOK" -> "Facebook"
        "EMAIL" -> "Email"
        "PASSKEY" -> "Passkey"
        else -> "Sequo"
    }
