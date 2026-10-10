package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import dev.orestegabo.sequovendor.model.formatCfaOrDash
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorErrorKind
import dev.orestegabo.sequovendor.ui.components.VendorErrorPanel
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.catalog_available
import sequovendor.shared.generated.resources.catalog_camera_verified
import sequovendor.shared.generated.resources.catalog_category_all
import sequovendor.shared.generated.resources.catalog_detail
import sequovendor.shared.generated.resources.catalog_empty
import sequovendor.shared.generated.resources.catalog_edit
import sequovendor.shared.generated.resources.catalog_editor_availability
import sequovendor.shared.generated.resources.catalog_editor_bargaining_disabled
import sequovendor.shared.generated.resources.catalog_editor_cancel
import sequovendor.shared.generated.resources.catalog_editor_category
import sequovendor.shared.generated.resources.catalog_editor_detail
import sequovendor.shared.generated.resources.catalog_editor_food_policy
import sequovendor.shared.generated.resources.catalog_editor_local_badge
import sequovendor.shared.generated.resources.catalog_editor_name
import sequovendor.shared.generated.resources.catalog_editor_original_price
import sequovendor.shared.generated.resources.catalog_editor_photo_policy
import sequovendor.shared.generated.resources.catalog_editor_photo_status
import sequovendor.shared.generated.resources.catalog_editor_prep_time
import sequovendor.shared.generated.resources.catalog_editor_price
import sequovendor.shared.generated.resources.catalog_editor_return_eligible
import sequovendor.shared.generated.resources.catalog_editor_save
import sequovendor.shared.generated.resources.catalog_editor_saved
import sequovendor.shared.generated.resources.catalog_editor_stock_count
import sequovendor.shared.generated.resources.catalog_editor_stock_state
import sequovendor.shared.generated.resources.catalog_editor_subcategory
import sequovendor.shared.generated.resources.catalog_editor_title
import sequovendor.shared.generated.resources.catalog_filter_all
import sequovendor.shared.generated.resources.catalog_last_updated
import sequovendor.shared.generated.resources.catalog_loading
import sequovendor.shared.generated.resources.catalog_needs_photo
import sequovendor.shared.generated.resources.catalog_out_of_stock
import sequovendor.shared.generated.resources.catalog_paused
import sequovendor.shared.generated.resources.catalog_price
import sequovendor.shared.generated.resources.catalog_refresh
import sequovendor.shared.generated.resources.catalog_search
import sequovendor.shared.generated.resources.catalog_status_active
import sequovendor.shared.generated.resources.catalog_status_archived
import sequovendor.shared.generated.resources.catalog_status_draft
import sequovendor.shared.generated.resources.catalog_status_paused
import sequovendor.shared.generated.resources.catalog_stock
import sequovendor.shared.generated.resources.catalog_title

@Composable
internal fun CatalogContent(
    products: List<CatalogProduct>,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onSaveProductDraft: (CatalogProduct) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<CatalogStatusFilter?>(null) }
    var editingProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var localSavedNotice by remember { mutableStateOf(false) }
    val categories = remember(products) {
        products.mapNotNull { it.category?.takeIf(String::isNotBlank) }.distinct().sorted()
    }
    val filteredProducts = products
        .filter { product ->
            query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.detail.orEmpty().contains(query, ignoreCase = true) ||
                product.subcategory.orEmpty().contains(query, ignoreCase = true)
        }
        .filter { product -> selectedCategory == null || product.category == selectedCategory }
        .filter { product -> selectedStatus?.matches(product.status) ?: true }

    LazyColumn(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.catalog_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.catalog_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (localSavedNotice) {
                    Text(appText(Res.string.catalog_editor_saved), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        editingProduct?.let { product ->
            item {
                ProductEditorCard(
                    product = product,
                    onCancel = { editingProduct = null },
                    onSave = { updated ->
                        onSaveProductDraft(updated)
                        editingProduct = null
                        localSavedNotice = true
                    },
                )
            }
        }

        if (isOffline) {
            item {
                VendorErrorPanel(VendorErrorKind.Offline, onRetry = onRefresh)
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it.take(80) },
                        label = { Text(appText(Res.string.catalog_search)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text(appText(Res.string.catalog_category_all)) },
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { selectedStatus = null },
                                label = { Text(appText(Res.string.catalog_filter_all)) },
                            )
                        }
                        items(CatalogStatusFilter.entries) { filter ->
                            FilterChip(
                                selected = selectedStatus == filter,
                                onClick = { selectedStatus = filter },
                                label = { Text(filter.label()) },
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isLoading) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator()
                                Text(appText(Res.string.catalog_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Text("${filteredProducts.size}/${products.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = onRefresh, enabled = !isLoading) {
                            Text(appText(Res.string.catalog_refresh))
                        }
                    }
                }
            }
        }

        if (!isLoading && filteredProducts.isEmpty()) {
            item {
                VendorCard {
                    Text(
                        appText(Res.string.catalog_empty),
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(filteredProducts, key = { it.id }) { product ->
            ProductCard(product, onEdit = { editingProduct = product })
        }
    }
}

@Composable
private fun ProductCard(product: CatalogProduct, onEdit: () -> Unit) {
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    product.detail?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(product.status.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (product.cameraVerified) appText(Res.string.catalog_camera_verified) else appText(Res.string.catalog_needs_photo),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.cameraVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                product.subcategory?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            KeyValueRow(appText(Res.string.catalog_price), product.priceCfa.formatCfaOrDash())
            KeyValueRow(appText(Res.string.catalog_stock), product.stockSummary ?: product.status.stockLabel())
            KeyValueRow(appText(Res.string.catalog_last_updated), product.updatedAt.formatIsoTimestampOrDash())
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text(appText(Res.string.catalog_edit))
            }
        }
    }
}

@Composable
private fun ProductEditorCard(
    product: CatalogProduct,
    onCancel: () -> Unit,
    onSave: (CatalogProduct) -> Unit,
) {
    var name by remember(product.id) { mutableStateOf(product.name) }
    var category by remember(product.id) { mutableStateOf(product.category.orEmpty()) }
    var subcategory by remember(product.id) { mutableStateOf(product.subcategory.orEmpty()) }
    var detail by remember(product.id) { mutableStateOf(product.detail.orEmpty()) }
    var price by remember(product.id) { mutableStateOf(product.priceCfa?.toString().orEmpty()) }
    var originalPrice by remember(product.id) { mutableStateOf(product.originalPriceCfa?.toString().orEmpty()) }
    var available by remember(product.id) { mutableStateOf(product.status == CatalogProductStatus.ACTIVE) }
    var stockCount by remember(product.id) { mutableStateOf(product.stockCount?.toString().orEmpty()) }
    val defaultStockState = product.stockSummary ?: product.status.stockLabel()
    var stockState by remember(product.id, defaultStockState) { mutableStateOf(defaultStockState) }
    var prepTime by remember(product.id) { mutableStateOf(product.preparationTimeMinutes?.toString().orEmpty()) }
    var returnEligible by remember(product.id) { mutableStateOf(product.returnEligible) }
    val canSave = name.isNotBlank()

    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(appText(Res.string.catalog_editor_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(appText(Res.string.catalog_editor_local_badge), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Switch(checked = available, onCheckedChange = { available = it })
            }
            Text(appText(Res.string.catalog_editor_photo_policy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(appText(Res.string.catalog_editor_food_policy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = name, onValueChange = { name = it.take(180) }, label = { Text(appText(Res.string.catalog_editor_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = sanitizeCatalogText(it, 80) }, label = { Text(appText(Res.string.catalog_editor_category)) }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = subcategory, onValueChange = { subcategory = sanitizeCatalogText(it, 80) }, label = { Text(appText(Res.string.catalog_editor_subcategory)) }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = detail, onValueChange = { detail = it.take(500) }, label = { Text(appText(Res.string.catalog_editor_detail)) }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = price, onValueChange = { price = sanitizeDigits(it, 9) }, label = { Text(appText(Res.string.catalog_editor_price)) }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = originalPrice, onValueChange = { originalPrice = sanitizeDigits(it, 9) }, label = { Text(appText(Res.string.catalog_editor_original_price)) }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = stockCount, onValueChange = { stockCount = sanitizeDigits(it, 6) }, label = { Text(appText(Res.string.catalog_editor_stock_count)) }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = stockState, onValueChange = { stockState = sanitizeCatalogText(it, 80) }, label = { Text(appText(Res.string.catalog_editor_stock_state)) }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = prepTime, onValueChange = { prepTime = sanitizeDigits(it, 4) }, label = { Text(appText(Res.string.catalog_editor_prep_time)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            KeyValueRow(appText(Res.string.catalog_editor_availability), if (available) appText(Res.string.catalog_available) else appText(Res.string.catalog_paused))
            KeyValueRow(appText(Res.string.catalog_editor_photo_status), if (product.cameraVerified) appText(Res.string.catalog_camera_verified) else appText(Res.string.catalog_needs_photo))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = returnEligible, onCheckedChange = { returnEligible = it })
                Text(appText(Res.string.catalog_editor_return_eligible))
            }
            Text(appText(Res.string.catalog_editor_bargaining_disabled), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(appText(Res.string.catalog_editor_cancel))
                }
                Button(
                    onClick = {
                        onSave(
                            product.copy(
                                name = name.trim(),
                                category = category.trim().ifBlank { null },
                                subcategory = subcategory.trim().ifBlank { null },
                                detail = detail.trim().ifBlank { null },
                                priceCfa = price.toIntOrNull(),
                                originalPriceCfa = originalPrice.toIntOrNull(),
                                status = if (available) CatalogProductStatus.ACTIVE else CatalogProductStatus.PAUSED,
                                stockCount = stockCount.toIntOrNull(),
                                stockSummary = stockState.trim().ifBlank { null },
                                preparationTimeMinutes = prepTime.toIntOrNull(),
                                returnEligible = returnEligible,
                                bargainingEnabled = false,
                            ),
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(appText(Res.string.catalog_editor_save))
                }
            }
        }
    }
}

private enum class CatalogStatusFilter {
    Active,
    Draft,
    Paused,
    Archived,
}

private fun CatalogStatusFilter.matches(status: CatalogProductStatus): Boolean =
    when (this) {
        CatalogStatusFilter.Active -> status == CatalogProductStatus.ACTIVE
        CatalogStatusFilter.Draft -> status == CatalogProductStatus.DRAFT
        CatalogStatusFilter.Paused -> status == CatalogProductStatus.INACTIVE || status == CatalogProductStatus.PAUSED
        CatalogStatusFilter.Archived -> status == CatalogProductStatus.ARCHIVED
    }

@Composable
private fun CatalogStatusFilter.label(): String =
    when (this) {
        CatalogStatusFilter.Active -> appText(Res.string.catalog_status_active)
        CatalogStatusFilter.Draft -> appText(Res.string.catalog_status_draft)
        CatalogStatusFilter.Paused -> appText(Res.string.catalog_status_paused)
        CatalogStatusFilter.Archived -> appText(Res.string.catalog_status_archived)
    }

@Composable
private fun CatalogProductStatus.label(): String =
    when (this) {
        CatalogProductStatus.ACTIVE -> appText(Res.string.catalog_status_active)
        CatalogProductStatus.INACTIVE,
        CatalogProductStatus.PAUSED,
        -> appText(Res.string.catalog_status_paused)
        CatalogProductStatus.DRAFT -> appText(Res.string.catalog_status_draft)
        CatalogProductStatus.ARCHIVED -> appText(Res.string.catalog_status_archived)
    }

@Composable
private fun CatalogProductStatus.stockLabel(): String =
    when (this) {
        CatalogProductStatus.ACTIVE -> appText(Res.string.catalog_available)
        CatalogProductStatus.INACTIVE,
        CatalogProductStatus.PAUSED,
        -> appText(Res.string.catalog_out_of_stock)
        CatalogProductStatus.DRAFT -> appText(Res.string.catalog_paused)
        CatalogProductStatus.ARCHIVED -> appText(Res.string.catalog_status_archived)
    }

private fun sanitizeDigits(input: String, maxLength: Int): String =
    input.filter(Char::isDigit).take(maxLength)

private fun sanitizeCatalogText(input: String, maxLength: Int): String =
    input.filter { it.isLetterOrDigit() || it.isWhitespace() || it in "-_.'/,&()" }.take(maxLength)
