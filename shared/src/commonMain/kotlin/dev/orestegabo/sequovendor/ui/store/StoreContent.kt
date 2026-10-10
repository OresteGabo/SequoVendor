package dev.orestegabo.sequovendor.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.StoreStatus
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.store_closed
import sequovendor.shared.generated.resources.store_customer_preview
import sequovendor.shared.generated.resources.store_detail
import sequovendor.shared.generated.resources.store_last_updated
import sequovendor.shared.generated.resources.store_local_state_notice
import sequovendor.shared.generated.resources.store_mark_closed
import sequovendor.shared.generated.resources.store_mark_open
import sequovendor.shared.generated.resources.store_open
import sequovendor.shared.generated.resources.store_operational_state
import sequovendor.shared.generated.resources.store_preview_closed
import sequovendor.shared.generated.resources.store_preview_closed_reason
import sequovendor.shared.generated.resources.store_preview_open
import sequovendor.shared.generated.resources.store_temporary_reason
import sequovendor.shared.generated.resources.store_temporary_reason_placeholder
import sequovendor.shared.generated.resources.store_title
import sequovendor.shared.generated.resources.store_weekday
import sequovendor.shared.generated.resources.store_weekend

@Composable
internal fun StoreContent(
    status: StoreStatus,
    closureReason: String,
    onOpenChange: (Boolean) -> Unit,
    onClosureReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val customerPreview = when {
        status.open -> appText(Res.string.store_preview_open)
        closureReason.isNotBlank() -> appText(Res.string.store_preview_closed_reason)
            .replace("%1\$s", closureReason)
        status.temporaryClosure?.reason?.isNotBlank() == true -> appText(Res.string.store_preview_closed_reason)
            .replace("%1\$s", status.temporaryClosure.reason.orEmpty())
        else -> appText(Res.string.store_preview_closed)
    }

    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(appText(Res.string.store_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(appText(Res.string.store_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        VendorCard {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(appText(Res.string.store_operational_state), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (status.open) appText(Res.string.store_open) else appText(Res.string.store_closed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        status.openStatus ?: if (status.open) "OPEN" else "PAUSED",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onOpenChange(true) },
                        enabled = !status.open,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(appText(Res.string.store_mark_open))
                    }
                    OutlinedButton(
                        onClick = { onOpenChange(false) },
                        enabled = status.open,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(appText(Res.string.store_mark_closed))
                    }
                }

                KeyValueRow(appText(Res.string.store_weekday), status.weekdayHours)
                KeyValueRow(appText(Res.string.store_weekend), status.weekendHours)
                KeyValueRow(appText(Res.string.store_last_updated), status.updatedAt.formatIsoTimestampOrDash())
            }
        }

        VendorCard {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(appText(Res.string.store_temporary_reason), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = closureReason,
                    onValueChange = { onClosureReasonChange(sanitizeClosureReason(it)) },
                    placeholder = { Text(appText(Res.string.store_temporary_reason_placeholder)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(appText(Res.string.store_local_state_notice), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        VendorCard {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(appText(Res.string.store_customer_preview), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(customerPreview, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun sanitizeClosureReason(input: String): String =
    input
        .filter { it.isAllowedClosureReasonChar() }
        .take(MaxClosureReasonLength)

private fun Char.isAllowedClosureReasonChar(): Boolean =
    isLetterOrDigit() ||
        isWhitespace() ||
        this in setOf('.', ',', '\'', '"', '’', '-', '/', '!', '?', ':', ';', '(', ')')

private const val MaxClosureReasonLength = 120
