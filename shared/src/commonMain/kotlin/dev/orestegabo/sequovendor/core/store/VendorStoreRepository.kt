package dev.orestegabo.sequovendor.core.store

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.model.OpeningHoursDay
import dev.orestegabo.sequovendor.model.OpeningHoursException
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.model.TemporaryClosure

interface VendorStoreRepository {
    suspend fun getAvailability(merchantId: String): ApiResult<StoreAvailabilityState>

    suspend fun setOpen(
        merchantId: String,
        open: Boolean,
        temporaryClosureReason: String? = null,
    ): ApiResult<StoreAvailabilityState>

    suspend fun setTemporaryClosureReason(
        merchantId: String,
        reason: String,
    ): ApiResult<StoreAvailabilityState>

    suspend fun setOpeningHours(
        merchantId: String,
        openingHours: List<OpeningHoursDay>,
    ): ApiResult<StoreAvailabilityState>

    suspend fun addTemporaryException(
        merchantId: String,
        exception: OpeningHoursException,
    ): ApiResult<StoreAvailabilityState>
}

class LocalVendorStoreRepository(
    initialAvailability: StoreAvailabilityState,
) : VendorStoreRepository {
    private var availability = initialAvailability

    override suspend fun getAvailability(merchantId: String): ApiResult<StoreAvailabilityState> =
        ApiResult.Success(availability.copy(merchantId = merchantId))

    override suspend fun setOpen(
        merchantId: String,
        open: Boolean,
        temporaryClosureReason: String?,
    ): ApiResult<StoreAvailabilityState> {
        availability = availability.copy(
            merchantId = merchantId,
            open = open,
            acceptingOrders = open,
            openStatus = if (open) "OPEN" else "PAUSED",
            temporaryClosure = if (open || temporaryClosureReason.isNullOrBlank()) {
                null
            } else {
                TemporaryClosure(
                    startsAt = "local",
                    endsAt = "local",
                    reason = temporaryClosureReason.trim(),
                )
            },
            updatedAt = LocalUpdatedAtLabel,
        )
        return ApiResult.Success(availability)
    }

    override suspend fun setTemporaryClosureReason(
        merchantId: String,
        reason: String,
    ): ApiResult<StoreAvailabilityState> {
        availability = availability.copy(
            merchantId = merchantId,
            temporaryClosure = reason.trim().takeIf { it.isNotBlank() }?.let {
                TemporaryClosure(
                    startsAt = "local",
                    endsAt = "local",
                    reason = it,
                )
            },
            updatedAt = LocalUpdatedAtLabel,
        )
        return ApiResult.Success(availability)
    }

    override suspend fun setOpeningHours(
        merchantId: String,
        openingHours: List<OpeningHoursDay>,
    ): ApiResult<StoreAvailabilityState> {
        availability = availability.copy(
            merchantId = merchantId,
            openingHours = openingHours.sortedBy { it.dayOfWeek },
            updatedAt = LocalUpdatedAtLabel,
        )
        return ApiResult.Success(availability)
    }

    override suspend fun addTemporaryException(
        merchantId: String,
        exception: OpeningHoursException,
    ): ApiResult<StoreAvailabilityState> {
        val normalized = exception.copy(
            id = exception.id ?: "local-exception-${availability.temporaryExceptions.size + 1}",
            opensAt = if (exception.closed) null else exception.opensAt,
            closesAt = if (exception.closed) null else exception.closesAt,
        )
        availability = availability.copy(
            merchantId = merchantId,
            temporaryExceptions = availability.temporaryExceptions + normalized,
            updatedAt = LocalUpdatedAtLabel,
        )
        return ApiResult.Success(availability)
    }

    private companion object {
        const val LocalUpdatedAtLabel = "Local update"
    }
}
