package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.auth.*
import dev.orestegabo.sequovendor.core.network.*
import dev.orestegabo.sequovendor.model.VendorSession
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlin.test.*

class VendorSessionConcurrencyTest {
    private val base = "http://127.0.0.1:55840"
    private val profile = """{"id":"user-a","provider":"GOOGLE","status":"ACTIVE","roles":["MERCHANT_OWNER"],"merchantScopeIds":["merchant-a"]}"""
    private fun reply(body: String, status: HttpStatusCode = HttpStatusCode.OK) = Pair(body, status)
    private class Store : AuthSessionStore {
        var session: AuthSession? = AuthSession("expired", "refresh-original", 1); var user: CurrentUser? = null
        override suspend fun save(session: AuthSession) { this.session = session }
        override suspend fun get() = session
        override suspend fun saveRememberedUser(user: CurrentUser) { this.user = user }
        override suspend fun getRememberedUser() = user
        override suspend fun clear() { session = null; user = null }
    }
    private class Tokens : SecureTokenStorage {
        var token: String? = null
        override suspend fun saveRefreshToken(token: String) { this.token = token }
        override suspend fun getRefreshToken() = token
        override suspend fun clearSession() { token = null }
    }
    private val biometrics = object : BiometricAuthenticator {
        override val availability = BiometricAvailability.NotSupported
        override suspend fun authenticate(config: BiometricPromptConfig) = BiometricAuthResult.Unavailable(availability)
    }
    @Test fun concurrentExpiredWorkspaceReadsRotateRefreshOnlyOnce() = runBlocking<Unit> {
        val store = Store(); var rotations = 0
        val client = createSequoVendorHttpClient(base, engine = MockEngine { request ->
            val (body, status) = when (request.url.encodedPath) {
                "/api/auth/refresh" -> { rotations++; delay(25); reply("""{"accessToken":"fresh","refreshToken":"refresh-new","expiresIn":3600}""") }
                else -> if (request.headers[HttpHeaders.Authorization] == "Bearer expired") reply("{}", HttpStatusCode.Unauthorized) else reply(profile)
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val repository = AuthRepository(AuthApiClient(base, client) { "vendor-device" }, store, SecureSessionCache(Tokens(), biometrics))
        try {
            val results = (1..6).map { async { repository.restoreSavedSession() } }.awaitAll()
            assertEquals(1, rotations); assertTrue(results.all { it?.id == "user-a" }); assertEquals("fresh", store.session?.accessToken)
        } finally { repository.close() }
    }
    @Test fun logoutCannotBeUndoneByAnInFlightRefresh() = runBlocking<Unit> {
        val store = Store(); val tokens = Tokens(); val started = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        val client = createSequoVendorHttpClient(base, engine = MockEngine { request ->
            val (body, status) = when (request.url.encodedPath) {
                "/api/auth/refresh" -> { started.complete(Unit); release.await(); reply("""{"accessToken":"fresh","refreshToken":"refresh-new","expiresIn":3600}""") }
                "/api/auth/logout" -> reply("{}")
                else -> if (request.headers[HttpHeaders.Authorization] == "Bearer expired") reply("{}", HttpStatusCode.Unauthorized) else reply(profile)
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val repository = AuthRepository(AuthApiClient(base, client) { "vendor-device" }, store, SecureSessionCache(tokens, biometrics))
        try {
            val restoring = async { repository.restoreSavedSession() }; started.await()
            val logout = async { repository.logout() }; yield(); release.complete(Unit)
            restoring.await(); logout.await()
            assertNull(store.session); assertNull(store.user); assertNull(tokens.token)
        } finally { repository.close() }
    }
    @Test fun expiredOldWorkspaceCannotRefreshOrSendTheNextAccountsToken() = runBlocking<Unit> {
        val expected = VendorSession("merchant-a", "A", "token-a", localMode = false, userId = "user-a")
        var current = expected; var refreshes = 0; val headers = mutableListOf<String?>()
        val client = createVendorWorkspaceClient(expected.workspaceKey(), { current }, { refreshes++; null }, { "token-b" }, {}, base,
            MockEngine { request ->
                headers += request.headers[HttpHeaders.Authorization]
                current = expected.copy(userId = "user-b", merchantId = "merchant-b", accessToken = "token-b")
                respond("{}", HttpStatusCode.Unauthorized)
            })
        try {
            assertFailsWith<SequoVendorApiException> { client.get("/api/merchant/sub-orders") }
            assertFailsWith<SequoVendorApiException> { client.get("/api/merchant/sub-orders") }
            assertEquals(listOf("Bearer token-a", null), headers); assertEquals(0, refreshes)
        } finally { client.close() }
    }
    @Test fun sameWorkspaceRefreshPreservesIdentityAndRequiresActiveMerchantScope() = runBlocking<Unit> {
        val initial = VendorSession("merchant-a", "A", "expired", localMode = false, userId = "user-a")
        var current = initial; val observed = mutableListOf<String?>()
        val verified = CurrentUser("user-a", provider = "GOOGLE", status = "ACTIVE", roles = setOf("MERCHANT_OWNER"), merchantScopeIds = setOf("merchant-a"))
        val client = createVendorWorkspaceClient(initial.workspaceKey(), { current }, { verified }, { "fresh" }, { current = current.copy(accessToken = it) }, base,
            MockEngine { request -> observed += request.headers[HttpHeaders.Authorization]; respond("{}", if (observed.size == 1) HttpStatusCode.Unauthorized else HttpStatusCode.OK) })
        try { client.get("/api/merchant/sub-orders"); assertEquals(listOf<String?>("Bearer expired", "Bearer fresh"), observed); assertEquals(initial.workspaceKey(), current.workspaceKey()) }
        finally { client.close() }
        assertTrue(verified.copy(status = "SUSPENDED").authorizedMerchantIds().isEmpty())
        assertNotEquals(initial.workspaceKey(), initial.copy(merchantId = "merchant-b").workspaceKey())
        assertNotEquals(initial.workspaceKey(), initial.copy(userId = "user-b").workspaceKey())
    }
    @Test fun cancellationIsNotPublishedAsAnOrdinaryNetworkFailure() = runBlocking<Unit> {
        assertFailsWith<CancellationException> { safeApiCall<Unit> { throw CancellationException("Local fixture left the screen") } }
    }
    @Test fun localFixtureEntryRequiresBuildLoopbackAndPositiveServerCapability() = runBlocking<Unit> {
        assertTrue(localNativeAuthAllowed("LOCAL_FIXTURE", base))
        for (url in listOf("https://api.sequoservice.com", "http://127.0.0.1.attacker.invalid", "http://user@127.0.0.1", "http://127.0.0.1/?target=external", "http://127.0.0.1:99999"))
            assertFalse(localNativeAuthAllowed("LOCAL_FIXTURE", url))
        assertFalse(localNativeAuthAllowed("PRODUCTION", base)); assertFalse(localNativeAuthAllowed("STAGING", base))
        var calls = 0
        val client = createSequoVendorHttpClient(base, engine = MockEngine { calls++; respond("{}") })
        val ordinary = AuthApiClient(base, client, environment = "PRODUCTION") { "vendor-device" }
        try { assertFalse(ordinary.localDevelopmentAccess().enabled); assertFailsWith<IllegalArgumentException> { ordinary.developmentSession("yawa") }; assertEquals(0, calls) }
        finally { ordinary.close() }
        var posts = 0
        val disabledClient = createSequoVendorHttpClient(base, engine = MockEngine { request ->
            if (request.method == HttpMethod.Post) posts++
            respond("""{"enabled":false,"mode":null,"accounts":[]}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val disabled = AuthApiClient(base, disabledClient, environment = "LOCAL_FIXTURE") { "vendor-device" }
        try { assertFailsWith<IllegalArgumentException> { disabled.developmentSession("yawa") }; assertEquals(0, posts) }
        finally { disabled.close() }
    }
    @Test fun localFixtureUsesNormalVerifiedSessionPersistenceWithoutRoleFallback() = runBlocking<Unit> {
        val store = Store().apply { session = null }; val tokens = Tokens(); val paths = mutableListOf<String>()
        var foreignResponse = false
        val client = createSequoVendorHttpClient(base, engine = MockEngine { request ->
            paths += request.url.encodedPath
            val body = when (request.url.encodedPath) {
                "/api/auth/development-access" -> {
                    assertEquals("SEQUO_VENDOR", request.url.parameters["appSource"])
                    """{"enabled":true,"mode":"LOCAL_FIXTURE","accounts":[{"key":"yawa","label":"Yawa — local test"}]}"""
                }
                "/api/auth/development-session" -> {
                    val raw = (request.body as io.ktor.http.content.TextContent).text
                    assertTrue(raw.contains("\"appSource\":\"SEQUO_VENDOR\"")); assertTrue(raw.contains("\"accountKey\":\"yawa\"")); assertFalse(raw.contains("password"))
                    """{"accessToken":"fixture-access","refreshToken":"fixture-refresh","expiresIn":3600,"user":${if (foreignResponse) profile.replace("user-a", "user-b") else profile}}"""
                }
                else -> { assertEquals("Bearer fixture-access", request.headers[HttpHeaders.Authorization]); profile }
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val repository = AuthRepository(AuthApiClient(base, client, environment = "LOCAL_FIXTURE") { "vendor-device" }, store, SecureSessionCache(tokens, biometrics))
        try {
            val user = repository.loginWithDevelopmentAccount("yawa")
            assertEquals("user-a", user.id); assertEquals(setOf("merchant-a"), user.authorizedMerchantIds())
            assertEquals("fixture-access", store.session?.accessToken); assertEquals("fixture-refresh", tokens.token)
            assertEquals(listOf("/api/auth/development-access", "/api/auth/development-session", "/api/auth/me"), paths)
            store.clear(); tokens.clearSession(); foreignResponse = true
            assertFailsWith<AuthApiException> { repository.loginWithDevelopmentAccount("yawa") }
            assertNull(store.session); assertNull(tokens.token)
        } finally { repository.close() }
    }

}
