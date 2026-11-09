package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogFoodOption
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.CatalogProductKind
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import dev.orestegabo.sequovendor.model.CatalogProductSyncState
import kotlinx.serialization.Serializable

@Serializable
data class CatalogProductDto(
    val id: String,
    val merchantId: String? = null,
    val name: String,
    val kind: String = "SellerSpecific",
    val status: String = "ACTIVE",
    val category: String? = null,
    val subcategory: String? = null,
    val detail: String? = null,
    val priceCfa: Int? = null,
    val optionHint: String? = null,
    val originalPriceCfa: Int? = null,
    val hasDiscount: Boolean = false,
    val bargainingEnabled: Boolean = false,
    val bargainFloorCfa: Int? = null,
    val cameraVerified: Boolean = false,
    val capturedAtLabel: String? = null,
    val photoRequired: Boolean = false,
    val imageUrl: String? = null,
    val customizations: List<CatalogOptionGroupDto> = emptyList(),
)

@Serializable
data class ManageCatalogProductRequest(
    val merchantId: String,
    val name: String,
    val kind: String = "SellerSpecific",
    val status: String = "ACTIVE",
    val category: String? = null,
    val subcategory: String? = null,
    val detail: String? = null,
    val basePriceCfa: Int? = null,
    val optionHint: String? = null,
    val originalPriceCfa: Int? = null,
    val bargainingEnabled: Boolean = false,
    val bargainFloorCfa: Int? = null,
    val cameraVerified: Boolean = false,
    val capturedAtLabel: String? = null,
    val sortOrder: Int = 0,
    val idempotencyKey: String? = null,
)

@Serializable
data class PatchCatalogProductRequest(
    val name: String? = null,
    val status: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val detail: String? = null,
    val basePriceCfa: Int? = null,
    val optionHint: String? = null,
    val originalPriceCfa: Int? = null,
    val bargainingEnabled: Boolean? = null,
    val bargainFloorCfa: Int? = null,
    val cameraVerified: Boolean? = null,
    val capturedAtLabel: String? = null,
    val sortOrder: Int? = null,
)

fun CatalogProductDto.toDomain(): CatalogProduct =
    CatalogProduct(
        id = id,
        name = name,
        priceCfa = priceCfa,
        status = status.toCatalogProductStatus(),
        cameraVerified = cameraVerified,
        merchantId = merchantId,
        kind = kind.toCatalogProductKind(),
        category = category,
        subcategory = subcategory,
        detail = detail,
        optionHint = optionHint,
        originalPriceCfa = originalPriceCfa,
        hasDiscount = hasDiscount,
        bargainingEnabled = bargainingEnabled,
        bargainFloorCfa = bargainFloorCfa,
        capturedAtLabel = capturedAtLabel,
        stockSummary = status.toStockSummary(),
        updatedAt = capturedAtLabel,
        stockCount = null,
        preparationTimeMinutes = null,
        returnEligible = false,
        syncState = CatalogProductSyncState.SERVER_SYNCED,
        foodOptionGroups = customizations.map { it.toDomain() },
        photoRequired = photoRequired,
        imageUrl = imageUrl,
    )

private fun String.toCatalogProductStatus(): CatalogProductStatus =
    when (uppercase()) {
        "ACTIVE" -> CatalogProductStatus.ACTIVE
        "INACTIVE" -> CatalogProductStatus.INACTIVE
        "DRAFT" -> CatalogProductStatus.DRAFT
        "PAUSED" -> CatalogProductStatus.PAUSED
        "ARCHIVED" -> CatalogProductStatus.ARCHIVED
        else -> CatalogProductStatus.INACTIVE
    }

private fun String.toCatalogProductKind(): CatalogProductKind =
    when (uppercase()) {
        "GENERICSEALEDITEM", "GENERIC_SEALED_ITEM" -> CatalogProductKind.GenericSealedItem
        else -> CatalogProductKind.SellerSpecific
    }

private fun String.toStockSummary(): String =
    when (uppercase()) {
        "ACTIVE" -> "Available"
        "DRAFT" -> "Draft"
        "ARCHIVED" -> "Archived"
        else -> "Paused"
    }

fun CatalogProduct.toManageCatalogProductRequest(merchantId: String): ManageCatalogProductRequest =
    ManageCatalogProductRequest(
        merchantId = merchantId,
        name = name,
        kind = kind.toApiValue(),
        status = status.toApiValue(),
        category = category,
        subcategory = subcategory,
        detail = detail,
        basePriceCfa = priceCfa,
        optionHint = optionHint,
        originalPriceCfa = originalPriceCfa,
        bargainingEnabled = bargainingEnabled,
        bargainFloorCfa = bargainFloorCfa,
        cameraVerified = cameraVerified,
        capturedAtLabel = capturedAtLabel,
        idempotencyKey = id.takeIf { it.isNotBlank() },
    )

fun CatalogProduct.toPatchCatalogProductRequest(): PatchCatalogProductRequest =
    PatchCatalogProductRequest(
        name = name,
        status = status.toApiValue(),
        category = category.orEmpty(),
        subcategory = subcategory.orEmpty(),
        detail = detail.orEmpty(),
        basePriceCfa = priceCfa,
        optionHint = optionHint,
        originalPriceCfa = originalPriceCfa,
        bargainingEnabled = bargainingEnabled,
        bargainFloorCfa = bargainFloorCfa,
        cameraVerified = cameraVerified,
        capturedAtLabel = capturedAtLabel,
    )

private fun CatalogProductKind.toApiValue(): String =
    when (this) {
        CatalogProductKind.SellerSpecific -> "SellerSpecific"
        CatalogProductKind.GenericSealedItem -> "GenericSealedItem"
    }

private fun CatalogProductStatus.toApiValue(): String =
    when (this) {
        CatalogProductStatus.ACTIVE -> "ACTIVE"
        CatalogProductStatus.INACTIVE,
        CatalogProductStatus.PAUSED,
        CatalogProductStatus.DRAFT,
        -> "INACTIVE"
        CatalogProductStatus.ARCHIVED -> "ARCHIVED"
    }

@Serializable
data class CatalogOptionDto(
    val id: String? = null,
    val name: String,
    val priceDeltaCfa: Int = 0,
    val sortOrder: Int = 0,
    val active: Boolean = true,
    val measureAmount: String? = null,
)

@Serializable
data class CatalogOptionGroupDto(
    val id: String? = null,
    val name: String,
    val minChoices: Int = 0,
    val maxChoices: Int = 1,
    val sortOrder: Int = 0,
    val active: Boolean = true,
    val options: List<CatalogOptionDto>,
    val type: String = "PREPARATION",
    val unit: String? = null,
)

@Serializable
internal data class SaveCatalogOptionsRequest(val groups: List<CatalogOptionGroupDto>)

internal fun CatalogOptionGroupDto.toDomain() = CatalogFoodOptionGroup(
    id = id.orEmpty(),
    name = name,
    required = minChoices > 0,
    maxSelections = maxChoices,
    minSelections = minChoices,
    active = active,
    options = options.map { CatalogFoodOption(it.id.orEmpty(), it.name, it.priceDeltaCfa, it.active, it.measureAmount) },
    type = type, unit = unit,
)

internal fun List<CatalogFoodOptionGroup>.toOptionsRequest() = SaveCatalogOptionsRequest(mapIndexed { index, group ->
    CatalogOptionGroupDto(
        id = group.id.ifBlank { null },
        name = group.name,
        minChoices = group.minSelections,
        maxChoices = group.maxSelections,
        sortOrder = index,
        active = group.active,
        type = group.type, unit = group.unit,
        options = group.options.mapIndexed { sort, option ->
            CatalogOptionDto(option.id.ifBlank { null }, option.label, option.priceDeltaCfa, sort, option.available, option.measureAmount)
        },
    )
})
