package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.catalog.CatalogProductReference
import dev.orestegabo.sequovendor.core.catalog.filterEssentials
import dev.orestegabo.sequovendor.feature.settings.appText
import kotlinx.coroutines.CancellationException
import sequovendor.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EssentialProductPicker(load: suspend () -> List<CatalogProductReference>, onDismiss: () -> Unit,
    onSelected: (CatalogProductReference) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<CatalogProductReference>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }
    val currentLoad by rememberUpdatedState(load)
    LaunchedEffect(retry) {
        loading = true; failed = false
        try { products = currentLoad() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    val matches = remember(products, query, category) { products.filterEssentials(query, category) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.88f).imePadding().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(appText(Res.string.catalog_essentials_title), style = MaterialTheme.typography.headlineSmall)
            Text(appText(Res.string.catalog_essentials_picker_hint), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(query, { query = it.take(180) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                label = { Text(appText(Res.string.catalog_essentials_search)) })
            if (!loading && !failed) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = category == null, onClick = { category = null }, label = { Text(appText(Res.string.catalog_essentials_all)) }) }
                items(listOf("grocery", "home", "beauty", "baby").filter { key -> products.any { it.category == key } }) { key ->
                    FilterChip(selected = category == key, onClick = { category = key }, label = { Text(appText(essentialCategoryLabel(key))) })
                }
            }
            when {
                loading -> CircularProgressIndicator()
                failed -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(appText(Res.string.catalog_essentials_failed))
                    OutlinedButton(onClick = { retry++ }) { Text(appText(Res.string.catalog_refresh)) }
                }
                matches.isEmpty() -> Text(appText(Res.string.catalog_essentials_empty), modifier = Modifier.weight(1f))
                else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
                    items(matches, key = { it.id }) { reference ->
                        ListItem(headlineContent = { Text(reference.name) },
                            supportingContent = { Text(appText(essentialCategoryLabel(reference.category))) },
                            modifier = Modifier.fillMaxWidth().clickable { onSelected(reference) })
                        HorizontalDivider()
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(appText(Res.string.catalog_editor_cancel))
            }
        }
    }
}

private fun essentialCategoryLabel(key: String) = when (key) {
    "grocery" -> Res.string.catalog_essentials_grocery
    "home" -> Res.string.catalog_essentials_home
    "beauty" -> Res.string.catalog_essentials_care
    "baby" -> Res.string.catalog_essentials_baby
    else -> Res.string.catalog_essentials_all
}
