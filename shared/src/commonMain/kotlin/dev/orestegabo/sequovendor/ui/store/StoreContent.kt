package dev.orestegabo.sequovendor.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.model.StoreStatus
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.store_closed
import sequovendor.shared.generated.resources.store_detail
import sequovendor.shared.generated.resources.store_open
import sequovendor.shared.generated.resources.store_title
import sequovendor.shared.generated.resources.store_weekday
import sequovendor.shared.generated.resources.store_weekend

@Composable
internal fun StoreContent(
    status: StoreStatus,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(appText(Res.string.store_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(appText(Res.string.store_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        VendorCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (status.open) appText(Res.string.store_open) else appText(Res.string.store_closed),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                KeyValueRow(appText(Res.string.store_weekday), status.weekdayHours)
                KeyValueRow(appText(Res.string.store_weekend), status.weekendHours)
            }
        }
    }
}
