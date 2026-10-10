package dev.orestegabo.sequovendor.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.VendorSection
import dev.orestegabo.sequovendor.ui.components.VendorChip
import dev.orestegabo.sequovendor.ui.components.VendorMark
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.app_name
import sequovendor.shared.generated.resources.app_subtitle
import sequovendor.shared.generated.resources.language_en
import sequovendor.shared.generated.resources.language_fr
import sequovendor.shared.generated.resources.nav_account
import sequovendor.shared.generated.resources.nav_catalog
import sequovendor.shared.generated.resources.nav_home
import sequovendor.shared.generated.resources.nav_orders
import sequovendor.shared.generated.resources.nav_store
import sequovendor.shared.generated.resources.theme_dark
import sequovendor.shared.generated.resources.theme_light
import sequovendor.shared.generated.resources.theme_system

@Composable
internal fun VendorTopBar(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                VendorMark("S")
                Column {
                    Text(appText(Res.string.app_name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        appText(Res.string.app_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VendorChip(
                    label = appText(Res.string.language_en),
                    selected = language == AppLanguage.English,
                    onClick = { onLanguageChange(AppLanguage.English) },
                )
                VendorChip(
                    label = appText(Res.string.language_fr),
                    selected = language == AppLanguage.French,
                    onClick = { onLanguageChange(AppLanguage.French) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            ThemeChip(AppThemePreference.System, themePreference, onThemePreferenceChange, Modifier.weight(1f))
            ThemeChip(AppThemePreference.Light, themePreference, onThemePreferenceChange, Modifier.weight(1f))
            ThemeChip(AppThemePreference.Dark, themePreference, onThemePreferenceChange, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ThemeChip(
    value: AppThemePreference,
    selected: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (value) {
        AppThemePreference.System -> appText(Res.string.theme_system)
        AppThemePreference.Light -> appText(Res.string.theme_light)
        AppThemePreference.Dark -> appText(Res.string.theme_dark)
    }
    VendorChip(label, selected == value, { onThemePreferenceChange(value) }, modifier)
}

@Composable
internal fun VendorNavigationBar(
    current: VendorSection,
    onSectionSelected: (VendorSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VendorSection.entries.forEach { section ->
            VendorChip(
                label = section.label(),
                selected = current == section,
                onClick = { onSectionSelected(section) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VendorSection.label(): String = when (this) {
    VendorSection.Home -> appText(Res.string.nav_home)
    VendorSection.Orders -> appText(Res.string.nav_orders)
    VendorSection.Catalog -> appText(Res.string.nav_catalog)
    VendorSection.Store -> appText(Res.string.nav_store)
    VendorSection.Account -> appText(Res.string.nav_account)
}

@Composable
internal fun MerchantIdentityRow(
    merchantName: String,
    merchantId: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(merchantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                merchantId,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        VendorChip(actionLabel, selected = false, onClick = onAction)
    }
}
