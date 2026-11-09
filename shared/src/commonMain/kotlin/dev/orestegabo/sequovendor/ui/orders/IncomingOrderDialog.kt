package dev.orestegabo.sequovendor.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.orestegabo.sequo.design.SequoActionButton
import dev.orestegabo.sequo.design.SequoDesign
import dev.orestegabo.sequovendor.core.orders.IncomingOrderPresentation
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.formatCfa
import sequovendor.shared.generated.resources.*

/** An incoming merchant order, using the existing server-owned accept/reject actions. */
@Composable
internal fun IncomingOrderDialog(
    presentation: IncomingOrderPresentation,
    onAccept: () -> Unit,
    onRefuse: () -> Unit,
    onSilence: () -> Unit,
) {
    val order = presentation.order
    Dialog(onDismissRequest = onSilence, properties = DialogProperties(
        usePlatformDefaultWidth = false, dismissOnClickOutside = false,
    )) {
        Box(Modifier.fillMaxSize().safeContentPadding(), contentAlignment = Alignment.Center) {
            Surface(Modifier.padding(SequoDesign.ScreenGutter).widthIn(max = 520.dp).fillMaxWidth(),
                shape = SequoDesign.CardShape, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null,
                        modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(appText(Res.string.incoming_order_title), style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(appText(Res.string.incoming_order_attention), style = MaterialTheme.typography.bodyMedium)
                    Text(order.displayReference, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(order.itemSubtotalCfa.formatCfa(), style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (order.lines.isEmpty()) Text(appText(Res.string.incoming_order_no_lines))
                        order.lines.forEach { line ->
                            Text("${line.quantity} × ${line.productName}", style = MaterialTheme.typography.bodyLarge)
                            line.optionLabel?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text(appText(Res.string.incoming_order_response_countdown)
                        .replace("{time}", responseCountdown(presentation.remainingResponseMillis)),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(appText(Res.string.incoming_order_ring_countdown)
                        .replace("{seconds}", ((presentation.remainingRingMillis + 999) / 1000).toString()),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SequoActionButton(onClick = onAccept, modifier = Modifier.fillMaxWidth()) {
                        Text(appText(Res.string.incoming_order_accept))
                    }
                    SequoActionButton(onClick = onRefuse, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError)) {
                        Text(appText(Res.string.incoming_order_refuse))
                    }
                    TextButton(onClick = onSilence) { Text(appText(Res.string.incoming_order_silence)) }
                    Text(appText(Res.string.incoming_order_deadline_notice), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun responseCountdown(milliseconds: Long): String {
    val seconds = ((milliseconds.coerceAtLeast(0) + 999) / 1000)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
