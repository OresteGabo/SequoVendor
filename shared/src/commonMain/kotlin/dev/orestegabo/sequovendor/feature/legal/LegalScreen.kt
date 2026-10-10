package dev.orestegabo.sequovendor.feature.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.legal_detail
import sequovendor.shared.generated.resources.legal_title

@Composable
internal fun LegalScreen(modifier: Modifier = Modifier) {
    VendorCard(modifier.padding(16.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(appText(Res.string.legal_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(appText(Res.string.legal_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
