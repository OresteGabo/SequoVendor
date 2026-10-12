package dev.orestegabo.sequovendor.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.auth.AdminLoginRequestSummary
import org.jetbrains.compose.resources.stringResource
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.admin_login_approve
import sequovendor.shared.generated.resources.admin_login_empty
import sequovendor.shared.generated.resources.admin_login_error
import sequovendor.shared.generated.resources.admin_login_expires
import sequovendor.shared.generated.resources.admin_login_requested
import sequovendor.shared.generated.resources.admin_login_reject
import sequovendor.shared.generated.resources.admin_login_refresh
import sequovendor.shared.generated.resources.admin_login_title
import sequovendor.shared.generated.resources.admin_login_warning

@Composable
fun AdminLoginRequestsScreen(
    requests: List<AdminLoginRequestSummary>,
    isLoading: Boolean,
    hasError: Boolean,
    onRefresh: () -> Unit,
    onApprove: (AdminLoginRequestSummary) -> Unit,
    onReject: (AdminLoginRequestSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.admin_login_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(Res.string.admin_login_warning), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (hasError) Text(stringResource(Res.string.admin_login_error), color = MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onRefresh, enabled = !isLoading) {
                        Text(stringResource(Res.string.admin_login_refresh))
                    }
                    if (isLoading) CircularProgressIndicator()
                }
            }
        }

        if (!isLoading && requests.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(Res.string.admin_login_empty),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(requests, key = { it.requestId }) { request ->
            AdminLoginRequestCard(
                request = request,
                onApprove = { onApprove(request) },
                onReject = { onReject(request) },
            )
        }
    }
}

@Composable
private fun AdminLoginRequestCard(
    request: AdminLoginRequestSummary,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(request.code, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            request.deviceLabel?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.titleMedium)
            }
            request.browserInfo?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${stringResource(Res.string.admin_login_requested)} ${request.requestedAt}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${stringResource(Res.string.admin_login_expires)} ${request.expiresAt}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) { Text(stringResource(Res.string.admin_login_approve)) }
                OutlinedButton(onClick = onReject) { Text(stringResource(Res.string.admin_login_reject)) }
            }
        }
    }
}
