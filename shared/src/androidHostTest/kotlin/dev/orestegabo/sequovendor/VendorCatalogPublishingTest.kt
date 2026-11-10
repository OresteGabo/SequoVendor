package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.catalog.CatalogApiClient
import dev.orestegabo.sequovendor.core.catalog.CatalogDraftState
import dev.orestegabo.sequovendor.core.catalog.NetworkVendorCatalogRepository
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogFoodOption
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.CatalogProductStatus
import dev.orestegabo.sequovendor.model.CatalogProductSyncState
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class VendorCatalogPublishingTest {
    private val baseUrl = "https://catalog.sequoservice.test"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val active = """{"id":"server-product","merchantId":"merchant-1","name":"Mango","priceCfa":750,"detail":"Fresh mango"}"""

    @Test
    fun sharedSuggestionsAndExplicitLinksSurviveCreateEditAndUnlink() = runBlocking {
        val reference = dev.orestegabo.sequovendor.core.catalog.CatalogProductReference("ref-milk", "Lait", "grocery", listOf("milk"))
        val response = active.dropLast(1) + """, "productReference":{"id":"ref-milk","name":"Lait","category":"grocery","aliases":["milk"]}}"""
        val sent = mutableListOf<String>()
        val engine = MockEngine { request ->
            if (request.method == HttpMethod.Get) {
                assertEquals("/api/catalog/product-references", request.url.encodedPath)
                assertEquals("lai", request.url.parameters["q"])
                assertEquals("6", request.url.parameters["limit"])
                respond("""[{"id":"ref-milk","name":"Lait","category":"grocery","aliases":["milk"]}]""", HttpStatusCode.OK, jsonHeaders)
            } else {
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                sent += body.getValue("productReferenceId").jsonPrimitive.content
                respond(response, HttpStatusCode.OK, jsonHeaders)
            }
        }
        createSequoVendorHttpClient(baseUrl, engine = engine).use { http ->
            val api = CatalogApiClient(baseUrl, http)
            assertEquals(reference, api.productSuggestions("lai").single())
            val product = CatalogProduct("local-product-ref", "Brand A 1 L", priceCfa = 1200, productReference = reference)
            assertEquals(reference, api.createMerchantProduct("merchant-1", product).productReference)
            api.updateMerchantProduct(product.copy(id = "server-product", merchantId = "merchant-1"))
            api.updateMerchantProduct(product.copy(id = "server-product", merchantId = "merchant-1", productReference = null, sharedReferenceCleared = true))
            assertEquals(listOf("ref-milk", "ref-milk", ""), sent)
        }
    }

    @Test
    fun optionSaveCarriesStableIdsConstraintsAndAvailability() = runBlocking {
        val json = """{"groups":[{"id":"side","name":"Side","minChoices":1,"options":[{"id":"plantain","name":"Plantain","priceDeltaCfa":500,"active":false}]}]}"""
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/api/merchant/catalog/products/server-product/customizations", request.url.encodedPath)
            assertEquals("merchant-1", request.url.parameters["merchantId"])
            assertEquals(Json.parseToJsonElement(json), Json.parseToJsonElement((request.body as TextContent).text))
            respond(Json.parseToJsonElement(json).jsonObject.getValue("groups").toString(), HttpStatusCode.OK, jsonHeaders)
        }
        createSequoVendorHttpClient(baseUrl, engine = engine).use { http ->
            val groups = listOf(CatalogFoodOptionGroup("side", "Side", required = true,
                options = listOf(CatalogFoodOption("plantain", "Plantain", 500, false))))
            assertEquals(groups, CatalogApiClient(baseUrl, http).saveOptions("merchant-1", "server-product", groups))
        }
    }

    @Test
    fun cameraUploadUsesMerchantScopedMultipartAndReadsSavedImage() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/merchant/catalog/products/server-product/photos/camera", request.url.encodedPath)
            assertEquals("merchant-1", request.url.parameters["merchantId"])
            assertEquals("fixture-device", request.url.parameters["deviceId"])
            assertTrue(!request.url.parameters["capturedAt"].isNullOrBlank())
            assertEquals("Bearer vendor-fixture", request.headers[HttpHeaders.Authorization])
            val multipart = assertIs<MultiPartFormDataContent>(request.body)
            assertTrue(multipart.contentType.toString().startsWith("multipart/form-data;"))
            request.headers[HttpHeaders.ContentType]?.let {
                assertEquals(multipart.contentType.toString(), it)
            }
            respond(
                active.dropLast(1) + ""","imageUrl":"/api/catalog/products/server-product/image","cameraVerified":true}""",
                HttpStatusCode.OK, jsonHeaders,
            )
        }
        createSequoVendorHttpClient(baseUrl, accessTokenProvider = { "vendor-fixture" }, engine = engine).use { http ->
            val product = CatalogApiClient(baseUrl, http).uploadCameraPhoto(
                "merchant-1", CatalogProduct("server-product", "Mango"), byteArrayOf(1, 2), "fixture-device",
            )
            assertEquals("/api/catalog/products/server-product/image", product.imageUrl)
            assertTrue(product.cameraVerified)
        }
    }

    @Test
    fun failedCreateRemainsUnconfirmedAndRetryReusesKey() = runBlocking {
        val keys = mutableListOf<String>()
        val engine = MockEngine { request ->
            if (request.method == HttpMethod.Get) {
                respond("[]", HttpStatusCode.OK, jsonHeaders)
            } else {
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/api/merchant/catalog/products", request.url.encodedPath)
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                keys += body.getValue("idempotencyKey").jsonPrimitive.content
                if (keys.size == 1) respond("{}", HttpStatusCode.ServiceUnavailable, jsonHeaders)
                else respond(active, HttpStatusCode.OK, jsonHeaders)
            }
        }
        createSequoVendorHttpClient(baseUrl, engine = engine).use { http ->
            val repository = NetworkVendorCatalogRepository(CatalogApiClient(baseUrl, http))
            val draft = CatalogProduct(
                id = "local-product-stable-retry",
                name = "Mango",
                priceCfa = 750,
                detail = "Fresh mango",
                syncState = CatalogProductSyncState.LOCAL_DRAFT,
            )
            val failed = assertIs<ApiResult.Failure>(repository.saveProduct("merchant-1", draft))
            assertEquals(ApiFailureKind.ServerUnavailable, failed.error.kind)
            assertTrue(assertIs<ApiResult.Success<*>>(repository.listProducts("merchant-1")).value == emptyList<CatalogProduct>())

            val retried = repository.saveProduct("merchant-1", draft)
            assertIs<ApiResult.Success<dev.orestegabo.sequovendor.core.catalog.VendorCatalogDraftResult>>(retried)
            assertEquals(CatalogDraftState.SERVER_SYNCED, retried.value.state)
            assertEquals("server-product", retried.value.product.id)
            assertEquals(listOf(draft.id, draft.id), keys)
        }
    }

    @Test
    fun editsSendAvailabilityAndBargainingInOneScopedPatch() = runBlocking {
        val statuses = mutableListOf<String>()
        val bargainingSettings = mutableListOf<Boolean>()
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("/api/merchant/catalog/products/server-product", request.url.encodedPath)
            assertEquals("merchant-1", request.url.parameters["merchantId"])
            val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            val status = body.getValue("status").jsonPrimitive.content
            statuses += status
            val bargainingEnabled = body.getValue("bargainingEnabled").jsonPrimitive.boolean
            bargainingSettings += bargainingEnabled
            assertEquals("Updated description", body.getValue("detail").jsonPrimitive.content)
            respond(active.dropLast(1) + ""","status":"$status","bargainingEnabled":$bargainingEnabled}""", HttpStatusCode.OK, jsonHeaders)
        }
        createSequoVendorHttpClient(baseUrl, engine = engine).use { http ->
            val repository = NetworkVendorCatalogRepository(CatalogApiClient(baseUrl, http))
            val edit = CatalogProduct("server-product", "Mango", detail = "Updated description")
            assertIs<ApiResult.Success<*>>(repository.saveProduct("merchant-1", edit.copy(status = CatalogProductStatus.PAUSED)))
            val enabled = assertIs<ApiResult.Success<dev.orestegabo.sequovendor.core.catalog.VendorCatalogDraftResult>>(
                repository.saveProduct("merchant-1", edit.copy(bargainingEnabled = true))
            )
            assertTrue(enabled.value.product.bargainingEnabled)
            assertEquals(listOf("INACTIVE", "ACTIVE"), statuses)
            assertEquals(listOf(false, true), bargainingSettings)
        }
    }
}
