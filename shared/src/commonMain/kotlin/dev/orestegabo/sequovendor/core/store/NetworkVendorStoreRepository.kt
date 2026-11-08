package dev.orestegabo.sequovendor.core.store

import dev.orestegabo.sequovendor.core.network.*
import dev.orestegabo.sequovendor.model.*
import kotlinx.coroutines.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Real sessions never turn a failed server read/save into a local open store. */
@OptIn(ExperimentalUuidApi::class)
class NetworkVendorStoreRepository(private val client: VendorStoreApiClient) : VendorStoreRepository {
    private val snapshots = mutableMapOf<String, StoreAvailabilityState>()
    private val pending = mutableMapOf<String, VendorStoreUpdateRequest>()

    override suspend fun getAvailability(merchantId: String): ApiResult<StoreAvailabilityState> = try {
        val actual = client.get(merchantId)
        snapshots[merchantId] = actual
        // This explicit fresh read is the review barrier before a different edit.
        pending.remove(merchantId)
        ApiResult.Success(actual)
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: Throwable) { snapshots.remove(merchantId); ApiResult.Failure(error.toApiFailure()) }

    override suspend fun setOpen(merchantId: String, open: Boolean, temporaryClosureReason: String?) =
        change(merchantId, if (open) "Merchant opened the store" else "Merchant paused new orders") {
            it.copy(open = open, acceptingOrders = open, closureReason = if (open) null else temporaryClosureReason?.trim()?.takeIf(String::isNotBlank))
        }

    override suspend fun setTemporaryClosureReason(merchantId: String, reason: String) =
        change(merchantId, "Merchant updated the store closure note") { it.copy(closureReason = reason.trim().takeIf(String::isNotBlank)) }

    override suspend fun setOpeningHours(merchantId: String, openingHours: List<OpeningHoursDay>) =
        change(merchantId, "Merchant updated weekly opening hours") { it.copy(openingHours = openingHours) }

    override suspend fun addTemporaryException(merchantId: String, exception: OpeningHoursException) =
        change(merchantId, "Merchant updated a dated opening exception") {
            it.copy(temporaryExceptions = it.temporaryExceptions.filterNot { old -> old.date == exception.date } + exception)
        }

    private suspend fun change(merchantId: String, reason: String,
        transform: (VendorStoreUpdateRequest) -> VendorStoreUpdateRequest): ApiResult<StoreAvailabilityState> {
        val state = snapshots[merchantId] ?: return ApiResult.Failure(ApiFailure(ApiFailureKind.Unexpected))
        if (pending.containsKey(merchantId)) return ApiResult.Failure(ApiFailure(ApiFailureKind.Unexpected, 409))
        val legacyAllows = state.canAcceptOrders == true
        val request = transform(VendorStoreUpdateRequest(
            if (state.configured) state.open else legacyAllows,
            if (state.configured) state.acceptingOrders else legacyAllows,
            state.timezone ?: "Africa/Lome", state.openingHours, state.temporaryExceptions,
            state.temporaryClosure, state.closureReason, reason, requireNotNull(state.updatedAt), Uuid.random().toString(),
        ))
        pending[merchantId] = request
        return submit(merchantId, request)
    }

    private suspend fun submit(merchantId: String, request: VendorStoreUpdateRequest): ApiResult<StoreAvailabilityState> = try {
        val mutation = client.update(merchantId, request)
        check(mutation.operation.idempotencyKey == request.idempotencyKey)
        val actual = client.get(merchantId)
        check(actual.history.any { it.idempotencyKey == request.idempotencyKey })
        snapshots[merchantId] = actual
        pending.remove(merchantId)
        ApiResult.Success(actual)
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: Throwable) { snapshots.remove(merchantId); ApiResult.Failure(error.toApiFailure()) }

    override fun hasPending(merchantId: String): Boolean = pending.containsKey(merchantId)
    override suspend fun retryPending(merchantId: String): ApiResult<StoreAvailabilityState> = pending[merchantId]
        ?.let { submit(merchantId, it) } ?: ApiResult.Failure(ApiFailure(ApiFailureKind.Unexpected))
    override fun close() = client.close()
}

fun createVendorStoreRepository(localMode: Boolean, initial: StoreAvailabilityState,
    apiClient: () -> VendorStoreApiClient): VendorStoreRepository =
    if (localMode) LocalVendorStoreRepository(initial) else NetworkVendorStoreRepository(apiClient())
