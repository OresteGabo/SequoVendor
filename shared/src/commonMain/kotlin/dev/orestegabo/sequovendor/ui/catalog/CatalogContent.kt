package dev.orestegabo.sequovendor.ui.catalog

import dev.orestegabo.sequo.design.SequoDesign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import dev.orestegabo.sequo.design.SequoActionButton as Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import dev.orestegabo.sequo.design.SequoOutlineButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import dev.orestegabo.sequovendor.core.catalog.CatalogProductReference
import sequovendor.shared.generated.resources.catalog_shared_hint
import sequovendor.shared.generated.resources.catalog_shared_selected
import sequovendor.shared.generated.resources.catalog_shared_unlink
import sequovendor.shared.generated.resources.catalog_shared_unavailable
import sequovendor.shared.generated.resources.catalog_shared_none
import sequovendor.shared.generated.resources.catalog_shared_loading
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.ui.navigation.VendorBackHandler
import dev.orestegabo.sequovendor.core.catalog.CatalogDraftState
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogProductKind
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.CatalogProductSyncState
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import dev.orestegabo.sequovendor.model.formatCfaOrDash
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorErrorKind
import dev.orestegabo.sequovendor.ui.components.VendorErrorPanel
import sequovendor.shared.generated.resources.Res
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import sequovendor.shared.generated.resources.catalog_standard_product
import sequovendor.shared.generated.resources.catalog_capture_photo
import sequovendor.shared.generated.resources.catalog_generic_photo
import sequovendor.shared.generated.resources.catalog_photo_pending
import sequovendor.shared.generated.resources.catalog_camera_unavailable
import sequovendor.shared.generated.resources.catalog_photo_attached
import sequovendor.shared.generated.resources.catalog_add
import sequovendor.shared.generated.resources.catalog_save_unconfirmed
import sequovendor.shared.generated.resources.catalog_available
import sequovendor.shared.generated.resources.catalog_camera_verified
import sequovendor.shared.generated.resources.catalog_category_all
import sequovendor.shared.generated.resources.catalog_detail
import sequovendor.shared.generated.resources.catalog_empty
import sequovendor.shared.generated.resources.catalog_edit
import sequovendor.shared.generated.resources.catalog_editor_availability
import sequovendor.shared.generated.resources.catalog_editor_bargaining
import sequovendor.shared.generated.resources.catalog_editor_bargaining_local_note
import sequovendor.shared.generated.resources.catalog_editor_cancel
import sequovendor.shared.generated.resources.catalog_editor_category
import sequovendor.shared.generated.resources.catalog_editor_detail
import sequovendor.shared.generated.resources.catalog_editor_food_policy
import sequovendor.shared.generated.resources.catalog_editor_name
import sequovendor.shared.generated.resources.catalog_editor_original_price
import sequovendor.shared.generated.resources.catalog_editor_photo_policy
import sequovendor.shared.generated.resources.catalog_editor_photo_status
import sequovendor.shared.generated.resources.catalog_editor_prep_time
import sequovendor.shared.generated.resources.catalog_editor_price
import sequovendor.shared.generated.resources.catalog_editor_return_eligible
import sequovendor.shared.generated.resources.catalog_editor_save
import sequovendor.shared.generated.resources.catalog_editor_saved
import sequovendor.shared.generated.resources.catalog_editor_saved_local
import sequovendor.shared.generated.resources.catalog_editor_saved_server
import sequovendor.shared.generated.resources.catalog_local_draft_badge
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
import sequovendor.shared.generated.resources.catalog_synced_badge

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun CatalogContent(
    products: List<CatalogProduct>,
    isLoading: Boolean,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    onSaveProduct: (CatalogProduct) -> Unit,
    loadProductSuggestions: suspend (String) -> List<CatalogProductReference> = { emptyList() },
    onSaveOptions: ((CatalogProduct, List<CatalogFoodOptionGroup>) -> Unit)?,
    onAttachPhoto: ((CatalogProduct, ByteArray?) -> Unit)?,
    saveInProgress: Boolean,
    lastSaveState: CatalogDraftState?,
    saveError: ApiFailure?,
    onClearSaveResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<CatalogStatusFilter?>(null) }
    var editingProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    val catalogList = rememberLazyListState()
    LaunchedEffect(editingProduct?.id) { catalogList.scrollToItem(0) }
    VendorBackHandler(enabled = editingProduct != null) {
        if (!saveInProgress) {
            editingProduct = null
            onClearSaveResult()
        }
    }
    LaunchedEffect(lastSaveState) {
        if (lastSaveState != null) editingProduct = null
    }
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

    LazyColumn(modifier.padding(horizontal = SequoDesign.ScreenGutter), state = catalogList, verticalArrangement = Arrangement.spacedBy(SequoDesign.SectionGap)) {
        if (editingProduct == null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(appText(Res.string.catalog_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        enabled = editingProduct == null && !saveInProgress,
                        onClick = {
                            onClearSaveResult()
                            editingProduct = CatalogProduct(
                                id = "local-product-${Uuid.random()}",
                                name = "",
                                syncState = CatalogProductSyncState.LOCAL_DRAFT,
                            )
                        },
                    ) { Text(appText(Res.string.catalog_add)) }
                    lastSaveState?.let { state ->
                        Text(
                            appText(state.savedLabel()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        editingProduct?.let { selected ->
            val product = products.firstOrNull { it.id == selected.id } ?: selected
            item {
                ProductEditorCard(
                    product = product,
                    loadProductSuggestions = loadProductSuggestions,
                    onCancel = {
                        editingProduct = null
                        onClearSaveResult()
                    },
                    onSave = onSaveProduct,
                    onSaveOptions = onSaveOptions?.let { save -> { groups -> save(product, groups) } },
                    onAttachPhoto = onAttachPhoto?.let { attach -> { jpeg -> attach(product, jpeg) } },
                    isSaving = saveInProgress,
                )
            }
        }

        saveError?.let { error ->
            item {
                Text(appText(Res.string.catalog_save_unconfirmed), color = MaterialTheme.colorScheme.error)
                Text(appText(error.userMessage), color = MaterialTheme.colorScheme.error)
            }
        }

        if (isOffline) {
            item {
                VendorErrorPanel(VendorErrorKind.Offline, onRetry = onRefresh)
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        modifier = Modifier.fillMaxWidth().padding(SequoDesign.CardPadding),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(filteredProducts, key = { it.id }) { product ->
            ProductCard(product, onEdit = {
                if (!saveInProgress) {
                    onClearSaveResult()
                    editingProduct = product
                }
            })
        }
    }
}

private fun CatalogProduct.photoStatusLabel() = when {
    cameraVerified -> Res.string.catalog_camera_verified
    imageUrl != null -> Res.string.catalog_photo_attached
    else -> Res.string.catalog_needs_photo
}

@Composable
private fun ProductCard(product: CatalogProduct, onEdit: () -> Unit) {
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    product.detail?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(Modifier.widthIn(max = 140.dp).padding(start = 10.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(product.status.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    Text(product.syncState.label(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    appText(product.photoStatusLabel()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.cameraVerified || product.imageUrl != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                product.subcategory?.takeIf { it.isNotBlank() }?.let {
                    Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            KeyValueRow(appText(Res.string.catalog_price), product.priceCfa.formatCfaOrDash())
            KeyValueRow(appText(Res.string.catalog_stock), product.stockSummary ?: product.status.stockLabel())
            KeyValueRow(
                appText(Res.string.catalog_last_updated),
                if (product.syncState == CatalogProductSyncState.LOCAL_DRAFT) {
                    appText(Res.string.catalog_local_draft_badge)
                } else {
                    product.updatedAt.formatIsoTimestampOrDash()
                },
            )
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text(appText(Res.string.catalog_edit))
            }
        }
    }
}

@Composable
private fun ProductEditorCard(
    product: CatalogProduct,
    loadProductSuggestions: suspend (String) -> List<CatalogProductReference>,
    onCancel: () -> Unit,
    onSave: (CatalogProduct) -> Unit,
    onSaveOptions: ((List<CatalogFoodOptionGroup>) -> Unit)?,
    onAttachPhoto: ((ByteArray?) -> Unit)?,
    isSaving: Boolean,
) {
    var kind by remember(product.id) { mutableStateOf(product.kind) }
    var cameraFailed by remember(product.id) { mutableStateOf(false) }
    val capture = rememberCatalogCamera { jpeg ->
        cameraFailed = jpeg == null
        jpeg?.let { onAttachPhoto?.invoke(it) }
    }
    var name by remember(product.id) { mutableStateOf(product.name) }
    var sharedReference by remember(product.id) { mutableStateOf(product.productReference) }
    var referenceCleared by remember(product.id) { mutableStateOf(product.sharedReferenceCleared) }
    var showSuggestions by remember(product.id) { mutableStateOf(product.productReference == null && product.name.isNotBlank()) }
    var suggestions by remember(product.id) { mutableStateOf<List<CatalogProductReference>>(emptyList()) }
    var suggestionsLoading by remember(product.id) { mutableStateOf(false) }
    var suggestionsFailed by remember(product.id) { mutableStateOf(false) }
    val suggest by rememberUpdatedState(loadProductSuggestions)
    LaunchedEffect(name, showSuggestions) {
        suggestions = emptyList(); suggestionsFailed = false; suggestionsLoading = false
        if (showSuggestions && name.trim().length >= 2) {
            suggestionsLoading = true
            delay(300)
            try { suggestions = suggest(name.trim()) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { suggestionsFailed = true }
            finally { suggestionsLoading = false }
        }
    }
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
    var bargainingEnabled by remember(product.id) { mutableStateOf(product.bargainingEnabled) }
    val priceRequired = product.name.isBlank() || product.priceCfa != null
    val canSave = name.isNotBlank() && (!priceRequired || price.toIntOrNull() != null)
    VendorCard {
        Column(Modifier.fillMaxWidth().padding(SequoDesign.CardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(appText(Res.string.catalog_editor_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(appText(Res.string.catalog_editor_saved), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Switch(
                    checked = available,
                    onCheckedChange = { available = it },
                    enabled = !isSaving && (!product.photoRequired || product.imageUrl != null),
                )
            }
            Text(appText(Res.string.catalog_editor_photo_policy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (product.name.isBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = kind == CatalogProductKind.GenericSealedItem,
                        onCheckedChange = { kind = if (it) CatalogProductKind.GenericSealedItem else CatalogProductKind.SellerSpecific },
                        enabled = !isSaving,
                    )
                    Text(appText(Res.string.catalog_standard_product))
                }
            }
            if (product.photoRequired && product.imageUrl == null) Text(appText(Res.string.catalog_photo_pending))
            if (onAttachPhoto != null && product.syncState == CatalogProductSyncState.SERVER_SYNCED) {
                if (product.kind == CatalogProductKind.GenericSealedItem) {
                    OutlinedButton(onClick = { onAttachPhoto(null) }, enabled = !isSaving) {
                        Text(appText(Res.string.catalog_generic_photo))
                    }
                } else {
                    OutlinedButton(onClick = capture, enabled = !isSaving) {
                        Text(appText(Res.string.catalog_capture_photo))
                    }
                    if (cameraFailed) Text(appText(Res.string.catalog_camera_unavailable), color = MaterialTheme.colorScheme.error)
                }
            }
            Text(appText(Res.string.catalog_editor_food_policy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = name, onValueChange = { name = it.take(180); showSuggestions = true }, label = { Text(appText(Res.string.catalog_editor_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text(appText(Res.string.catalog_shared_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            sharedReference?.let { reference ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(appText(Res.string.catalog_shared_selected).replace("{name}", reference.name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { sharedReference = null; referenceCleared = true; showSuggestions = false }, enabled = !isSaving) {
                        Text(appText(Res.string.catalog_shared_unlink))
                    }
                }
            }
            if (showSuggestions && name.trim().length >= 2) {
                when {
                    suggestionsLoading -> Text(appText(Res.string.catalog_shared_loading), style = MaterialTheme.typography.bodySmall)
                    suggestionsFailed -> Text(appText(Res.string.catalog_shared_unavailable), style = MaterialTheme.typography.bodySmall)
                    suggestions.isEmpty() -> Text(appText(Res.string.catalog_shared_none), style = MaterialTheme.typography.bodySmall)
                    else -> suggestions.forEach { reference ->
                        OutlinedButton(onClick = {
                            sharedReference = reference; referenceCleared = false; showSuggestions = false
                            name = reference.name
                            if (category.isBlank()) category = reference.category
                        }, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(reference.name, style = MaterialTheme.typography.titleSmall)
                                Text(reference.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
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
            if (onSaveOptions != null && product.syncState == CatalogProductSyncState.SERVER_SYNCED) {
                FoodOptionsEditor(product, isSaving, onSaveOptions)
            }
            KeyValueRow(appText(Res.string.catalog_editor_availability), if (available) appText(Res.string.catalog_available) else appText(Res.string.catalog_paused))
            KeyValueRow(appText(Res.string.catalog_editor_photo_status), appText(product.photoStatusLabel()))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = returnEligible, onCheckedChange = { returnEligible = it })
                Text(appText(Res.string.catalog_editor_return_eligible))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(appText(Res.string.catalog_editor_bargaining), style = MaterialTheme.typography.bodyMedium)
                    Text(appText(Res.string.catalog_editor_bargaining_local_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = bargainingEnabled, onCheckedChange = { bargainingEnabled = it })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, enabled = !isSaving, modifier = Modifier.weight(1f)) {
                    Text(appText(Res.string.catalog_editor_cancel))
                }
                Button(
                    onClick = {
                        onSave(
                            product.copy(
                                name = name.trim(),
                                productReference = sharedReference,
                                sharedReferenceCleared = referenceCleared,
                                kind = kind,
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
                                bargainingEnabled = bargainingEnabled,
                            ),
                        )
                    },
                    enabled = canSave && !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(appText(Res.string.catalog_editor_save))
                }
            }
        }
    }
}

@Composable
private fun CatalogProductSyncState.label(): String =
    when (this) {
        CatalogProductSyncState.SERVER_SYNCED -> appText(Res.string.catalog_synced_badge)
        CatalogProductSyncState.LOCAL_DRAFT -> appText(Res.string.catalog_local_draft_badge)
    }

private fun CatalogDraftState.savedLabel() =
    when (this) {
        CatalogDraftState.SERVER_SYNCED -> Res.string.catalog_editor_saved_server
        CatalogDraftState.LOCAL_DRAFT -> Res.string.catalog_editor_saved_local
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
