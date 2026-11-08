package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.core.notifications.FakeVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NetworkVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NotificationsApiClient
import dev.orestegabo.sequovendor.core.notifications.loadInboxSnapshot
import dev.orestegabo.sequovendor.model.DemoVendorData
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VendorNotificationClientTest {
    private val origin = "http://127.0.0.1:1"
    private fun message(family: String = "SEQUO_MERCHANT") = """
        {"id":"server-message","eventId":"event-fixture","recipientUserId":"vendor-fixture",
         "appFamily":"$family","eventType":"ORDER_CREATED","severity":"INFO",
         "title":"Server order","body":"A server notification","createdAt":"2026-11-08T10:00:00Z"}
    """.trimIndent()

    @Test
    fun emptyServerInboxStaysEmptyAndARejectedReadClearsServerRowsUntilRetry() = runBlocking<Unit> {
        var status = HttpStatusCode.OK
        var body = "[]"
        val client = createSequoVendorHttpClient(origin, accessTokenProvider = { "fixture-vendor" }, engine = MockEngine { request ->
            assertEquals("/api/notifications/inbox", request.url.encodedPath)
            assertEquals("Bearer fixture-vendor", request.headers[HttpHeaders.Authorization])
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val repository = NetworkVendorNotificationsRepository(NotificationsApiClient(origin, client))
        try {
            val empty = repository.loadInboxSnapshot()
            assertTrue(empty.notifications.isEmpty())
            assertFalse(empty.isOffline)
            body = "[${message()}]"
            assertEquals(listOf("server-message"), repository.loadInboxSnapshot().notifications.map { it.id })
            for (failure in listOf(HttpStatusCode.ServiceUnavailable, HttpStatusCode.Unauthorized)) {
                status = failure
                body = "{}"
                val rejected = repository.loadInboxSnapshot()
                assertTrue(rejected.notifications.isEmpty())
                assertTrue(rejected.isOffline)
            }
            status = HttpStatusCode.OK
            body = "[${message()}]"
            val recovered = repository.loadInboxSnapshot()
            assertEquals(listOf("server-message"), recovered.notifications.map { it.id })
            assertFalse(recovered.isOffline)
        } finally { client.close() }
    }

    @Test
    fun anotherAppFamilyDoesNotBecomeMerchantOrDemoAlerts() = runBlocking<Unit> {
        val client = createSequoVendorHttpClient(origin, engine = MockEngine {
            respond("[${message("SEQUO_CUSTOMER")}]", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val snapshot = NetworkVendorNotificationsRepository(NotificationsApiClient(origin, client)).loadInboxSnapshot()
            assertTrue(snapshot.notifications.isEmpty())
            assertFalse(snapshot.isOffline)
        } finally { client.close() }
    }

    @Test
    fun explicitDemoRetainsItsRepositoryAndDoesNotReseedAnArchivedInbox() = runBlocking<Unit> {
        val demo = DemoVendorData.notifications()
        val repository = FakeVendorNotificationsRepository(demo)
        assertEquals(demo, repository.loadInboxSnapshot().notifications)
        demo.forEach { repository.archive(it.id) }
        assertTrue(repository.loadInboxSnapshot().notifications.isEmpty())
        assertEquals(demo.size, repository.loadInboxSnapshot(includeArchived = true).notifications.size)
    }
}
