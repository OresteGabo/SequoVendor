package dev.orestegabo.sequovendor.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.LocalAppLanguage
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.ui.components.VendorTitleStatusRow
import sequovendor.shared.generated.resources.*

internal fun storeStatusText(status: StoreAvailabilityState, language: AppLanguage): String {
    val resource = when {
        status.openStatus == "UNAVAILABLE" -> Res.string.store_state_unavailable
        status.openStatus == "MERCHANT_INACTIVE" -> Res.string.store_state_inactive
        !status.configured || status.openStatus == "NOT_CONFIGURED" -> Res.string.store_state_setup
        else -> when (status.openStatus) {
            "OPEN" -> Res.string.store_state_open
            "PAUSED" -> Res.string.store_state_paused
            "NOT_ACCEPTING_ORDERS" -> Res.string.store_state_orders_paused
            "TEMPORARILY_CLOSED" -> Res.string.store_state_temporary
            "CLOSED_BY_EXCEPTION" -> Res.string.store_state_exception
            "OUTSIDE_HOURS" -> Res.string.store_state_outside
            null -> if (status.canAcceptOrders ?: status.open) Res.string.store_state_open else Res.string.store_state_paused
            else -> Res.string.store_state_unavailable
        }
    }
    return appText(resource, language)
}

@Composable
internal fun StoreStatusSummary(status: StoreAvailabilityState, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VendorTitleStatusRow(title, storeStatusText(status, LocalAppLanguage.current))
        Text(storeAvailabilityLabel(status), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
