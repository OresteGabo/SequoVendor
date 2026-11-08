package dev.orestegabo.sequovendor.ui.chrome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.orestegabo.sequo.design.SequoNavDestination
import dev.orestegabo.sequo.design.SequoNavigationBar
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.VendorSection
import sequovendor.shared.generated.resources.*

@Composable
internal fun VendorBottomBar(
    modifier: Modifier = Modifier,
    currentSection: VendorSection,
    onSectionSelected: (VendorSection) -> Unit,
) {
    val selected = if (currentSection == VendorSection.Store) VendorSection.Account else currentSection
    SequoNavigationBar(
        destinations = VendorPrimarySections.map { SequoNavDestination(it.name, it.title(), it.icon()) },
        selectedKey = selected.name,
        onSelect = { key -> VendorPrimarySections.firstOrNull { it.name == key }?.let(onSectionSelected) },
        modifier = modifier,
    )
}

private val VendorPrimarySections = listOf(
    VendorSection.Dashboard,
    VendorSection.Orders,
    VendorSection.Catalog,
    VendorSection.Account,
)

@Composable
private fun VendorSection.title(): String = when (this) {
    VendorSection.Dashboard -> appText(Res.string.nav_dashboard)
    VendorSection.Orders -> appText(Res.string.nav_orders)
    VendorSection.Catalog -> appText(Res.string.nav_catalog)
    VendorSection.Store -> appText(Res.string.nav_store)
    VendorSection.Notifications -> appText(Res.string.nav_notifications)
    VendorSection.Account -> appText(Res.string.nav_account)
}

private fun VendorSection.icon() = when (this) {
    VendorSection.Dashboard -> Icons.Filled.Home
    VendorSection.Orders -> Icons.Filled.ReceiptLong
    VendorSection.Catalog -> Icons.Filled.Inventory2
    VendorSection.Store -> Icons.Filled.Storefront
    VendorSection.Notifications -> Icons.Filled.Notifications
    VendorSection.Account -> Icons.Filled.Person
}
