package dev.orestegabo.sequovendor.core.catalog

import dev.orestegabo.sequovendor.model.CatalogProduct
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
        foodOptionGroups = emptyList(),
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
