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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import sequovendor.shared.generated.resources.account_offline
import sequovendor.shared.generated.resources.account_open_notifications
import sequovendor.shared.generated.resources.account_online
import sequovendor.shared.generated.resources.account_payment_card
import sequovendor.shared.generated.resources.account_payment_cash
import sequovendor.shared.generated.resources.account_payment_mobile_money
import sequovendor.shared.generated.resources.account_payments_accepted
import sequovendor.shared.generated.resources.account_profile
import sequovendor.shared.generated.resources.account_provider
import sequovendor.shared.generated.resources.account_status
import sequovendor.shared.generated.resources.account_theme
import sequovendor.shared.generated.resources.account_title
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

@Composable
private fun ApiHealthStatus?.healthLabel(): String =
    if (this?.healthy == true) {
        val status = actuatorStatus?.takeIf { it.isNotBlank() } ?: "UP"
        "${appText(Res.string.account_api_healthy)} ($status)"
    } else {
        appText(Res.string.account_api_unreachable)
    }
