package dev.orestegabo.sequovendor.ui.store

import androidx.compose.runtime.Composable
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import sequovendor.shared.generated.resources.*

@Composable
internal fun storeAvailabilityLabel(status: StoreAvailabilityState): String = when {
    status.openStatus == "MERCHANT_INACTIVE" -> appText(Res.string.store_state_inactive)
    status.openStatus == "UNAVAILABLE" -> appText(Res.string.store_status_unavailable)
    !status.configured -> appText(Res.string.store_not_configured)
    status.canAcceptOrders ?: status.open -> appText(Res.string.store_open)
    else -> appText(Res.string.store_closed)
}
