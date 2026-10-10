package dev.orestegabo.sequovendor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.error_offline_detail
import sequovendor.shared.generated.resources.error_offline_title
import sequovendor.shared.generated.resources.error_session_detail
import sequovendor.shared.generated.resources.error_session_title
import sequovendor.shared.generated.resources.orders_retry

internal enum class VendorErrorKind {
    Offline,
    SessionExpired,
}

@Composable
internal fun VendorErrorPanel(
    kind: VendorErrorKind,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    val title = when (kind) {
        VendorErrorKind.Offline -> appText(Res.string.error_offline_title)
        VendorErrorKind.SessionExpired -> appText(Res.string.error_session_title)
    }
    val detail = when (kind) {
        VendorErrorKind.Offline -> appText(Res.string.error_offline_detail)
        VendorErrorKind.SessionExpired -> appText(Res.string.error_session_detail)
    }
    VendorCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
            VendorPrimaryButton(appText(Res.string.orders_retry), onRetry)
        }
    }
}
