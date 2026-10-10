package dev.orestegabo.sequovendor.ui.chrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.designsystem.component.SequoShapes
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.VendorSection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.nav_account
import sequovendor.shared.generated.resources.nav_catalog
import sequovendor.shared.generated.resources.nav_dashboard
import sequovendor.shared.generated.resources.nav_notifications
import sequovendor.shared.generated.resources.nav_orders
import sequovendor.shared.generated.resources.nav_store

@Composable
internal fun VendorBottomBar(
    modifier: Modifier = Modifier,
    currentSection: VendorSection,
    onSectionSelected: (VendorSection) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SequoShapes.NavContainer)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface.copy(alpha = 0.98f),
                            colorScheme.surfaceContainer.copy(alpha = 0.94f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = SequoShapes.NavContainer,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                VendorPrimarySections.forEach { section ->
                    VendorBottomNavItem(
                        modifier = Modifier.weight(1f),
                        section = section,
                        selected = currentSection == section,
                        onClick = { onSectionSelected(section) },
                    )
                }
            }
        }
    }
}

private val VendorPrimarySections = listOf(
    VendorSection.Dashboard,
    VendorSection.Orders,
    VendorSection.Catalog,
    VendorSection.Store,
    VendorSection.Account,
)

@Composable
private fun VendorBottomNavItem(
    section: VendorSection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val label = section.title()
    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primaryContainer else Color.Transparent,
        label = "vendorNavItemContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
        label = "vendorNavItemContent",
    )

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = SequoShapes.NavItem,
        tonalElevation = if (selected) 3.dp else 0.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(SequoShapes.IconCapsule)
                    .background(if (selected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = section.icon(),
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(width = 16.dp, height = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) colorScheme.primary else Color.Transparent),
            )
        }
    }
}

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
