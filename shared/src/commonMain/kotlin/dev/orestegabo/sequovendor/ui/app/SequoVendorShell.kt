package dev.orestegabo.sequovendor.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.core.auth.AuthApiClient
import dev.orestegabo.sequovendor.core.auth.AuthApiException
import dev.orestegabo.sequovendor.core.auth.AuthRepository
import dev.orestegabo.sequovendor.core.auth.AuthUserMessage
import dev.orestegabo.sequovendor.core.auth.CurrentUser
import dev.orestegabo.sequovendor.core.auth.GoogleSignInFailureReason
import dev.orestegabo.sequovendor.core.auth.GoogleSignInResult
import dev.orestegabo.sequovendor.core.auth.SecureSessionCache
import dev.orestegabo.sequovendor.core.auth.rememberAuthDeviceId
import dev.orestegabo.sequovendor.core.auth.rememberAuthSessionStore
import dev.orestegabo.sequovendor.core.auth.rememberBiometricAuthenticator
import dev.orestegabo.sequovendor.core.auth.rememberSecureTokenStorage
import dev.orestegabo.sequovendor.core.catalog.CatalogApiClient
import dev.orestegabo.sequovendor.core.catalog.FakeVendorCatalogRepository
import dev.orestegabo.sequovendor.core.catalog.NetworkVendorCatalogRepository
import dev.orestegabo.sequovendor.core.catalog.VendorCatalogRepository
import dev.orestegabo.sequovendor.core.fulfillment.FakeMerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentApiClient
import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.NetworkMerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.VendorOrderAction
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiHealthClient
import dev.orestegabo.sequovendor.core.network.ApiHealthStatus
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.core.notifications.FakeVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NetworkVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NotificationsApiClient
import dev.orestegabo.sequovendor.core.notifications.VendorNotificationsRepository
import dev.orestegabo.sequovendor.core.store.LocalVendorStoreRepository
import dev.orestegabo.sequovendor.core.store.VendorStoreRepository
import dev.orestegabo.sequovendor.feature.auth.AuthScreen
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.DemoVendorData
import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.OpeningHoursDay
import dev.orestegabo.sequovendor.model.OpeningHoursException
import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType
import dev.orestegabo.sequovendor.model.VendorSection
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.ui.account.AccountContent
import dev.orestegabo.sequovendor.ui.catalog.CatalogContent
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorChip
import dev.orestegabo.sequovendor.ui.components.VendorMark
import dev.orestegabo.sequovendor.ui.chrome.VendorBottomBar
import dev.orestegabo.sequovendor.ui.dashboard.DashboardConnectionState
import dev.orestegabo.sequovendor.ui.dashboard.DashboardContent
import dev.orestegabo.sequovendor.ui.notifications.NotificationsContent
import dev.orestegabo.sequovendor.ui.orders.OrdersContent
import dev.orestegabo.sequovendor.ui.store.StoreContent
import kotlinx.coroutines.launch
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.app_name
import sequovendor.shared.generated.resources.app_subtitle
import sequovendor.shared.generated.resources.auth_error_account_other_method
import sequovendor.shared.generated.resources.auth_error_email_format
import sequovendor.shared.generated.resources.auth_error_email_password
import sequovendor.shared.generated.resources.auth_error_google_could_not_verify
import sequovendor.shared.generated.resources.auth_error_google_expired
import sequovendor.shared.generated.resources.auth_error_google_failed
import sequovendor.shared.generated.resources.auth_error_google_in_progress
import sequovendor.shared.generated.resources.auth_error_google_interrupted
import sequovendor.shared.generated.resources.auth_error_google_missing_email
import sequovendor.shared.generated.resources.auth_error_google_not_configured
import sequovendor.shared.generated.resources.auth_error_google_unavailable
import sequovendor.shared.generated.resources.auth_error_google_unverified_email
import sequovendor.shared.generated.resources.auth_error_profile_load_failed
import sequovendor.shared.generated.resources.auth_error_session_restore_failed
import sequovendor.shared.generated.resources.auth_error_social_failed
import sequovendor.shared.generated.resources.auth_error_social_unavailable
import sequovendor.shared.generated.resources.auth_error_too_many_attempts
import sequovendor.shared.generated.resources.auth_error_unavailable
import sequovendor.shared.generated.resources.auth_merchant_required
import sequovendor.shared.generated.resources.auth_not_ready
import sequovendor.shared.generated.resources.cd_notifications
import sequovendor.shared.generated.resources.cd_open_menu
import sequovendor.shared.generated.resources.drawer_guest
import sequovendor.shared.generated.resources.drawer_language
import sequovendor.shared.generated.resources.drawer_merchant
import sequovendor.shared.generated.resources.drawer_quick_links
import sequovendor.shared.generated.resources.drawer_signed_in
import sequovendor.shared.generated.resources.drawer_support
import sequovendor.shared.generated.resources.drawer_support_detail
import sequovendor.shared.generated.resources.drawer_theme
import sequovendor.shared.generated.resources.drawer_theme_dark
import sequovendor.shared.generated.resources.drawer_theme_light
import sequovendor.shared.generated.resources.drawer_theme_system
import sequovendor.shared.generated.resources.language_en
import sequovendor.shared.generated.resources.language_fr
import sequovendor.shared.generated.resources.nav_catalog
import sequovendor.shared.generated.resources.nav_catalog_subtitle
import sequovendor.shared.generated.resources.nav_dashboard
import sequovendor.shared.generated.resources.nav_dashboard_subtitle
import sequovendor.shared.generated.resources.nav_notifications
import sequovendor.shared.generated.resources.nav_notifications_subtitle
import sequovendor.shared.generated.resources.nav_orders
import sequovendor.shared.generated.resources.nav_orders_subtitle
import sequovendor.shared.generated.resources.nav_account
import sequovendor.shared.generated.resources.nav_account_subtitle
import sequovendor.shared.generated.resources.nav_store
import sequovendor.shared.generated.resources.nav_store_subtitle
import sequovendor.shared.generated.resources.shell_sign_out

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SequoVendorShell(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    onGoogleSignIn: suspend () -> GoogleSignInResult,
    onGoogleSignOut: suspend () -> Unit,
) {
    val deviceId = rememberAuthDeviceId()
    val sessionStore = rememberAuthSessionStore()
    val secureTokenStorage = rememberSecureTokenStorage()
    val biometricAuthenticator = rememberBiometricAuthenticator()
    val authRepository = remember(deviceId, sessionStore, secureTokenStorage, biometricAuthenticator) {
        AuthRepository(
            authApiClient = AuthApiClient(deviceIdProvider = { deviceId }),
            sessionStore = sessionStore,
            secureSessionCache = SecureSessionCache(secureTokenStorage, biometricAuthenticator),
        )
    }
    val apiHealthClient = remember { ApiHealthClient() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val notReadyMessage = appText(Res.string.auth_not_ready)
    val authError = { error: Throwable -> userFacingAuthError(error, notReadyMessage, language) }
    val googleError = { reason: GoogleSignInFailureReason -> appText(reason.stringResource(), language) }

    var session by remember { mutableStateOf<VendorSession?>(null) }
    var rememberedUser by remember { mutableStateOf<CurrentUser?>(null) }
    var authNotice by remember { mutableStateOf<String?>(null) }
    var emailAuthInProgress by remember { mutableStateOf(false) }
    var googleSignInInProgress by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf(VendorSection.Dashboard) }
    var apiHealthStatus by remember { mutableStateOf<ApiHealthStatus?>(null) }

    val localOrders = remember { DemoVendorData.orders() }
    val localProducts = remember { DemoVendorData.products() }
    val localNotifications = remember { DemoVendorData.notifications() }
    var products by remember { mutableStateOf(localProducts) }
    var notifications by remember { mutableStateOf(localNotifications) }
    var notificationPreference by remember { mutableStateOf(VendorNotificationPreference()) }
    var store by remember { mutableStateOf(DemoVendorData.store()) }
    var closureNote by remember { mutableStateOf("") }
    suspend fun clearSavedSessionForDifferentAccount() {
        if (rememberedUser != null || authRepository.getSavedSession() != null) {
            runCatching { authRepository.logout() }
            runCatching { onGoogleSignOut() }
            rememberedUser = null
            session = null
        }
    }

    LaunchedEffect(authRepository) {
        rememberedUser = authRepository.getRememberedUser()
        runCatching { authRepository.restoreSavedSession() }
            .onSuccess { user ->
                if (user != null) {
                    rememberedUser = user
                    val savedSession = authRepository.getSavedSession()
                    session = VendorSession(
                        merchantId = user.id,
                        displayName = user.displayName,
                        accessToken = savedSession?.accessToken,
                        authProvider = user.provider,
                        localMode = false,
                        userId = user.id,
                        email = user.email,
                        status = user.status,
                    )
                }
            }
    }

    if (session == null) {
        AuthScreen(
            language = language,
            onLanguageChange = onLanguageChange,
            emailAuthInProgress = emailAuthInProgress,
            googleSignInInProgress = googleSignInInProgress,
            rememberedUser = rememberedUser,
            onContinueRememberedUser = {
                scope.launch {
                    authRepository.getSavedSession()?.let { saved ->
                        rememberedUser?.let { user ->
                            session = VendorSession(
                                merchantId = user.id,
                                displayName = user.displayName,
                                accessToken = saved.accessToken,
                                authProvider = user.provider,
                                localMode = false,
                                userId = user.id,
                                email = user.email,
                                status = user.status,
                            )
                        }
                    } ?: run { authNotice = notReadyMessage }
                }
            },
            onEmailLogin = { email, password ->
                scope.launch {
                    emailAuthInProgress = true
                    runCatching {
                        clearSavedSessionForDifferentAccount()
                        authRepository.loginWithEmail(email, password)
                        authRepository.currentUser()
                    }.onSuccess { user ->
                        if (user != null) {
                            rememberedUser = user
                            authRepository.rememberUser(user)
                            session = VendorSession(
                                merchantId = user.id,
                                displayName = user.displayName,
                                accessToken = authRepository.getSavedSession()?.accessToken,
                                authProvider = user.provider,
                                localMode = false,
                                userId = user.id,
                                email = user.email,
                                status = user.status,
                            )
                        } else {
                            authNotice = notReadyMessage
                        }
                    }.onFailure { error ->
                        authNotice = authError(error)
                    }
                    emailAuthInProgress = false
                }
            },
            onEmailSignUp = { email, password, name ->
                scope.launch {
                    emailAuthInProgress = true
                    runCatching {
                        clearSavedSessionForDifferentAccount()
                        authRepository.signUpWithEmail(email, password, name)
                        authRepository.currentUser()
                    }.onSuccess { user ->
                        if (user != null) {
                            rememberedUser = user
                            authRepository.rememberUser(user)
                            session = VendorSession(
                                merchantId = user.id,
                                displayName = user.displayName,
                                accessToken = authRepository.getSavedSession()?.accessToken,
                                authProvider = user.provider,
                                localMode = false,
                                userId = user.id,
                                email = user.email,
                                status = user.status,
                            )
                        } else {
                            authNotice = notReadyMessage
                        }
                    }.onFailure { error ->
                        authNotice = authError(error)
                    }
                    emailAuthInProgress = false
                }
            },
            onGoogleLogin = {
                if (googleSignInInProgress) return@AuthScreen
                scope.launch {
                    googleSignInInProgress = true
                    authNotice = null
                    clearSavedSessionForDifferentAccount()
                    val result = runCatching { onGoogleSignIn() }
                        .getOrElse { GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed) }
                    when (result) {
                        is GoogleSignInResult.Success -> {
                            runCatching {
                                authRepository.loginWithGoogle(result.idToken)
                                authRepository.currentUser()
                            }.onSuccess { user ->
                                if (user != null) {
                                    rememberedUser = user
                                    authRepository.rememberUser(user)
                                    session = VendorSession(
                                        merchantId = user.id,
                                        displayName = user.displayName,
                                        accessToken = authRepository.getSavedSession()?.accessToken,
                                        authProvider = user.provider,
                                        localMode = false,
                                        userId = user.id,
                                        email = user.email,
                                        status = user.status,
                                    )
                                } else {
                                    authNotice = notReadyMessage
                                }
                            }.onFailure { error ->
                                authNotice = authError(error)
                            }
                        }
                        GoogleSignInResult.Cancelled -> Unit
                        is GoogleSignInResult.Failure -> {
                            authNotice = googleError(result.reason)
                        }
                    }
                    googleSignInInProgress = false
                }
            },
            onAppleLogin = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    authNotice = notReadyMessage
                }
            },
            onFacebookLogin = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    authNotice = notReadyMessage
                }
            },
            onWhatsAppLogin = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    authNotice = notReadyMessage
                }
            },
            onPasskeyLogin = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    authNotice = notReadyMessage
                }
            },
            onSequoLogin = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    authNotice = notReadyMessage
                }
            },
            onSkipAuth = {
                scope.launch {
                    clearSavedSessionForDifferentAccount()
                    session = DemoVendorData.session()
                }
            },
            onPrivacyTermsClick = { authNotice = notReadyMessage },
            modifier = Modifier.fillMaxSize(),
        )
        authNotice?.let { notice ->
            Box(Modifier.fillMaxSize().safeContentPadding().padding(18.dp), contentAlignment = Alignment.BottomCenter) {
                VendorCard {
                    Text(
                        notice,
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }

    val activeSession = requireNotNull(session)
    var dashboardOrders by remember { mutableStateOf<List<MerchantSubOrder>>(localOrders) }
    var dashboardConnectionState by remember { mutableStateOf(DashboardConnectionState.LocalFallback) }
    var ordersLoading by remember { mutableStateOf(false) }
    var orderActionInProgressId by remember { mutableStateOf<String?>(null) }
    var orderActionFailed by remember { mutableStateOf(false) }
    var detailOrder by remember { mutableStateOf<MerchantSubOrder?>(null) }
    var detailSla by remember { mutableStateOf<MerchantSlaInfo?>(null) }
    var detailLoading by remember { mutableStateOf(false) }
    var detailOffline by remember { mutableStateOf(false) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogOffline by remember { mutableStateOf(false) }
    var notificationsLoading by remember { mutableStateOf(false) }
    var notificationsOffline by remember { mutableStateOf(false) }
    var notificationActionFailed by remember { mutableStateOf(false) }
    var notificationPreferenceSaved by remember { mutableStateOf(false) }
    var includeArchivedNotifications by remember { mutableStateOf(false) }
    val storeRepository: VendorStoreRepository = remember { LocalVendorStoreRepository(store) }
    val catalogRepository: VendorCatalogRepository = remember(activeSession.accessToken, activeSession.localMode) {
        if (activeSession.localMode || activeSession.accessToken.isNullOrBlank()) {
            FakeVendorCatalogRepository(localProducts)
        } else {
            NetworkVendorCatalogRepository(
                CatalogApiClient(
                    httpClient = createSequoVendorHttpClient(
                        accessTokenProvider = { session?.accessToken },
                        refreshAccessToken = {
                            val restoredUser = runCatching { authRepository.restoreSavedSession() }.getOrNull()
                            val refreshedToken = authRepository.getSavedSession()?.accessToken
                            if (restoredUser != null && !refreshedToken.isNullOrBlank()) {
                                session = session?.copy(accessToken = refreshedToken)
                                true
                            } else {
                                false
                            }
                        },
                    ),
                ),
            )
        }
    }
    val fulfillmentRepository: MerchantFulfillmentRepository = remember(activeSession.accessToken, activeSession.localMode) {
        if (activeSession.localMode || activeSession.accessToken.isNullOrBlank()) {
            FakeMerchantFulfillmentRepository(localOrders)
        } else {
            NetworkMerchantFulfillmentRepository(
                MerchantFulfillmentApiClient(
                    httpClient = createSequoVendorHttpClient(
                        accessTokenProvider = { session?.accessToken },
                        refreshAccessToken = {
                            val restoredUser = runCatching { authRepository.restoreSavedSession() }.getOrNull()
                            val refreshedToken = authRepository.getSavedSession()?.accessToken
                            if (restoredUser != null && !refreshedToken.isNullOrBlank()) {
                                session = session?.copy(accessToken = refreshedToken)
                                true
                            } else {
                                false
                            }
                        },
                    ),
                ),
            )
        }
    }
    val notificationsRepository: VendorNotificationsRepository = remember(activeSession.accessToken, activeSession.localMode) {
        if (activeSession.localMode || activeSession.accessToken.isNullOrBlank()) {
            FakeVendorNotificationsRepository(localNotifications)
        } else {
            NetworkVendorNotificationsRepository(
                NotificationsApiClient(
                    httpClient = createSequoVendorHttpClient(
                        accessTokenProvider = { session?.accessToken },
                        refreshAccessToken = {
                            val restoredUser = runCatching { authRepository.restoreSavedSession() }.getOrNull()
                            val refreshedToken = authRepository.getSavedSession()?.accessToken
                            if (restoredUser != null && !refreshedToken.isNullOrBlank()) {
                                session = session?.copy(accessToken = refreshedToken)
                                true
                            } else {
                                false
                            }
                        },
                    ),
                ),
            )
        }
    }
    val setStoreOpen: (Boolean) -> Unit = { open ->
        scope.launch {
            when (val result = storeRepository.setOpen(activeSession.merchantId, open, closureNote)) {
                is ApiResult.Success -> store = result.value
                is ApiResult.Failure -> Unit
            }
        }
    }

    fun checkApiHealth() {
        scope.launch {
            apiHealthStatus = apiHealthClient.checkHealth()
        }
    }
    val setClosureReason: (String) -> Unit = { note ->
        closureNote = note
        scope.launch {
            when (val result = storeRepository.setTemporaryClosureReason(activeSession.merchantId, note)) {
                is ApiResult.Success -> store = result.value
                is ApiResult.Failure -> Unit
            }
        }
    }
    val setOpeningHours: (List<OpeningHoursDay>) -> Unit = { openingHours ->
        scope.launch {
            when (val result = storeRepository.setOpeningHours(activeSession.merchantId, openingHours)) {
                is ApiResult.Success -> store = result.value
                is ApiResult.Failure -> Unit
            }
        }
    }
    val addTemporaryException: (OpeningHoursException) -> Unit = { exception ->
        scope.launch {
            when (val result = storeRepository.addTemporaryException(activeSession.merchantId, exception)) {
                is ApiResult.Success -> store = result.value
                is ApiResult.Failure -> Unit
            }
        }
    }

    suspend fun refreshMerchantOrders(showLoading: Boolean = true) {
        if (showLoading) ordersLoading = true
        when (val result = fulfillmentRepository.listSubOrders(activeSession.merchantId)) {
            is ApiResult.Success -> {
                dashboardOrders = result.value
                dashboardConnectionState = if (activeSession.localMode) {
                    DashboardConnectionState.LocalFallback
                } else {
                    DashboardConnectionState.Connected
                }
            }
            is ApiResult.Failure -> {
                dashboardOrders = localOrders
                dashboardConnectionState = DashboardConnectionState.ErrorFallback
            }
        }
        ordersLoading = false
    }

    suspend fun refreshCatalog(showLoading: Boolean = true) {
        if (showLoading) catalogLoading = true
        when (val result = catalogRepository.listProducts(activeSession.merchantId)) {
            is ApiResult.Success -> {
                products = result.value.ifEmpty { localProducts }
                catalogOffline = false
            }
            is ApiResult.Failure -> {
                products = localProducts
                catalogOffline = true
            }
        }
        catalogLoading = false
    }

    suspend fun refreshNotifications(showLoading: Boolean = true) {
        if (showLoading) notificationsLoading = true
        notificationActionFailed = false
        when (val result = notificationsRepository.inbox(includeArchivedNotifications)) {
            is ApiResult.Success -> {
                notifications = result.value.ifEmpty { localNotifications }
                notificationsOffline = false
            }
            is ApiResult.Failure -> {
                notifications = localNotifications
                notificationsOffline = true
            }
        }
        when (val preferenceResult = notificationsRepository.effectivePreference(VendorNotificationPreferenceEventType.ORDER_CREATED)) {
            is ApiResult.Success -> notificationPreference = preferenceResult.value.copy(eventType = VendorNotificationPreferenceEventType.ALL)
            is ApiResult.Failure -> Unit
        }
        notificationsLoading = false
    }

    fun replaceNotification(updated: VendorNotification) {
        notifications = notifications.map { if (it.id == updated.id) updated else it }
    }

    fun handleNotificationAction(
        notification: VendorNotification,
        action: suspend (String) -> ApiResult<VendorNotification>,
    ) {
        scope.launch {
            notificationActionFailed = false
            notificationPreferenceSaved = false
            when (val result = action(notification.id)) {
                is ApiResult.Success -> replaceNotification(result.value)
                is ApiResult.Failure -> notificationActionFailed = true
            }
        }
    }

    fun saveNotificationPreference(preference: VendorNotificationPreference) {
        scope.launch {
            notificationActionFailed = false
            notificationPreferenceSaved = false
            when (val result = notificationsRepository.savePreference(preference)) {
                is ApiResult.Success -> {
                    notificationPreference = result.value
                    notificationPreferenceSaved = true
                }
                is ApiResult.Failure -> notificationActionFailed = true
            }
        }
    }

    fun saveProductDraft(product: CatalogProduct) {
        scope.launch {
            when (val result = catalogRepository.saveDraftLocally(activeSession.merchantId, product)) {
                is ApiResult.Success -> {
                    products = products.filterNot { it.id == result.value.product.id } + result.value.product
                    catalogOffline = false
                }
                is ApiResult.Failure -> {
                    catalogOffline = true
                }
            }
        }
    }

    fun replaceMerchantOrder(updated: MerchantSubOrder) {
        dashboardOrders = dashboardOrders.map { order ->
            if (order.id == updated.id) updated else order
        }
    }

    suspend fun loadOrderDetail(
        subOrderId: String,
        fallback: MerchantSubOrder? = null,
        showLoading: Boolean = true,
    ) {
        if (showLoading) detailLoading = true
        detailOffline = false
        fallback?.let { detailOrder = it }

        when (val orderResult = fulfillmentRepository.getSubOrder(subOrderId, activeSession.merchantId)) {
            is ApiResult.Success -> {
                detailOrder = orderResult.value
                replaceMerchantOrder(orderResult.value)
                when (val slaResult = fulfillmentRepository.getSla(subOrderId, activeSession.merchantId)) {
                    is ApiResult.Success -> detailSla = slaResult.value
                    is ApiResult.Failure -> detailSla = orderResult.value.sla
                }
            }
            is ApiResult.Failure -> {
                detailOffline = true
                detailSla = fallback?.sla
            }
        }
        detailLoading = false
    }

    fun handleOrderAction(
        order: MerchantSubOrder,
        action: VendorOrderAction,
        reason: String?,
    ) {
        if (orderActionInProgressId != null) return
        orderActionInProgressId = order.id
        scope.launch {
            orderActionFailed = false
            val result = when (action) {
                VendorOrderAction.Accept -> fulfillmentRepository.accept(order.id, activeSession.merchantId)
                VendorOrderAction.StartPreparation -> fulfillmentRepository.startPreparation(order.id, activeSession.merchantId)
                VendorOrderAction.MarkPacked -> fulfillmentRepository.markPacked(
                    subOrderId = order.id,
                    merchantId = activeSession.merchantId,
                    packageCount = reason?.toIntOrNull()?.coerceAtLeast(1) ?: order.packageCount.coerceAtLeast(1),
                )
                VendorOrderAction.Handoff -> fulfillmentRepository.handoff(order.id, activeSession.merchantId)
                VendorOrderAction.Reject -> fulfillmentRepository.reject(
                    subOrderId = order.id,
                    merchantId = activeSession.merchantId,
                    reason = reason.orEmpty(),
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    replaceMerchantOrder(result.value)
                    if (detailOrder?.id == result.value.id) {
                        detailOrder = result.value
                        loadOrderDetail(result.value.id, fallback = result.value, showLoading = false)
                    }
                    dashboardConnectionState = if (activeSession.localMode) {
                        DashboardConnectionState.LocalFallback
                    } else {
                        DashboardConnectionState.Connected
                    }
                }
                is ApiResult.Failure -> {
                    orderActionFailed = true
                    dashboardConnectionState = DashboardConnectionState.ErrorFallback
                }
            }
            orderActionInProgressId = null
        }
    }

    LaunchedEffect(fulfillmentRepository, activeSession.merchantId) {
        refreshMerchantOrders(showLoading = true)
    }

    LaunchedEffect(catalogRepository, activeSession.merchantId) {
        refreshCatalog(showLoading = true)
    }

    LaunchedEffect(notificationsRepository, includeArchivedNotifications) {
        refreshNotifications(showLoading = true)
    }

    LaunchedEffect(Unit) {
        checkApiHealth()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            VendorDrawer(
                session = activeSession,
                language = language,
                onLanguageChange = onLanguageChange,
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
                currentSection = currentSection,
                onSectionSelected = { section ->
                    currentSection = section
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        onGoogleSignOut()
                        session = null
                        drawerState.close()
                    }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                VendorTopAppBar(
                    section = currentSection,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNotificationsClick = { currentSection = VendorSection.Notifications },
                )
            },
            bottomBar = {
                VendorBottomBar(currentSection = currentSection, onSectionSelected = { currentSection = it })
            },
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    when (currentSection) {
                        VendorSection.Dashboard -> DashboardContent(
                            session = activeSession,
                            orders = dashboardOrders,
                            products = products,
                            store = store,
                            connectionState = dashboardConnectionState,
                            onStoreOpenChange = setStoreOpen,
                            onOrdersClick = { currentSection = VendorSection.Orders },
                            onCatalogClick = { currentSection = VendorSection.Catalog },
                            onStoreClick = { currentSection = VendorSection.Store },
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Orders -> OrdersContent(
                            orders = dashboardOrders,
                            isLoading = ordersLoading,
                            isOffline = dashboardConnectionState == DashboardConnectionState.ErrorFallback,
                            actionInProgressOrderId = orderActionInProgressId,
                            actionFailed = orderActionFailed,
                            detailOrder = detailOrder,
                            detailSla = detailSla,
                            detailLoading = detailLoading,
                            detailOffline = detailOffline,
                            onOpenDetail = { order ->
                                scope.launch {
                                    loadOrderDetail(order.id, fallback = order)
                                }
                            },
                            onCloseDetail = {
                                detailOrder = null
                                detailSla = null
                                detailOffline = false
                            },
                            onRefresh = { scope.launch { refreshMerchantOrders(showLoading = true) } },
                            onOrderAction = ::handleOrderAction,
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Catalog -> CatalogContent(
                            products = products,
                            isLoading = catalogLoading,
                            isOffline = catalogOffline,
                            onRefresh = { scope.launch { refreshCatalog(showLoading = true) } },
                            onSaveProductDraft = ::saveProductDraft,
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Store -> StoreContent(
                            status = store,
                            closureReason = closureNote,
                            onOpenChange = setStoreOpen,
                            onClosureReasonChange = setClosureReason,
                            onOpeningHoursChange = setOpeningHours,
                            onAddTemporaryException = addTemporaryException,
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Notifications -> NotificationsContent(
                            notifications = notifications,
                            preference = notificationPreference,
                            isLoading = notificationsLoading,
                            isOffline = notificationsOffline,
                            actionFailed = notificationActionFailed,
                            preferenceSaved = notificationPreferenceSaved,
                            includeArchived = includeArchivedNotifications,
                            onIncludeArchivedChange = { includeArchivedNotifications = it },
                            onRefresh = { scope.launch { refreshNotifications(showLoading = true) } },
                            onMarkRead = { notification -> handleNotificationAction(notification, notificationsRepository::markRead) },
                            onArchive = { notification -> handleNotificationAction(notification, notificationsRepository::archive) },
                            onUnarchive = { notification -> handleNotificationAction(notification, notificationsRepository::unarchive) },
                            onPreferenceChange = ::saveNotificationPreference,
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Account -> AccountContent(
                            session = activeSession,
                            availability = store,
                            language = language,
                            onLanguageChange = onLanguageChange,
                            themePreference = themePreference,
                            onThemePreferenceChange = onThemePreferenceChange,
                            apiHealthStatus = apiHealthStatus,
                            onCheckApiHealth = ::checkApiHealth,
                            onAvailabilityChange = setStoreOpen,
                            onStoreClick = { currentSection = VendorSection.Store },
                            onNotificationsClick = { currentSection = VendorSection.Notifications },
                            onLogout = {
                                scope.launch {
                                    authRepository.logout()
                                    onGoogleSignOut()
                                    session = null
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorTopAppBar(
    section: VendorSection,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(section.title(), fontWeight = FontWeight.Bold)
                Text(section.subtitle(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = appText(Res.string.cd_open_menu))
            }
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Filled.Notifications, contentDescription = appText(Res.string.cd_notifications))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun VendorDrawer(
    session: VendorSession,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    currentSection: VendorSection,
    onSectionSelected: (VendorSection) -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                VendorMark("S")
                Column {
                    Text(session.displayName.ifBlank { appText(Res.string.drawer_merchant) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (session.localMode) appText(Res.string.drawer_guest) else appText(Res.string.drawer_signed_in),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DrawerSectionTitle(appText(Res.string.drawer_quick_links))
            VendorDrawerSections.forEach { section ->
                DrawerActionRow(section.icon(), section.title(), selected = currentSection == section) { onSectionSelected(section) }
            }

            DrawerSectionTitle(appText(Res.string.drawer_language))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VendorChip(appText(Res.string.language_en), language == AppLanguage.English, { onLanguageChange(AppLanguage.English) }, Modifier.weight(1f))
                VendorChip(appText(Res.string.language_fr), language == AppLanguage.French, { onLanguageChange(AppLanguage.French) }, Modifier.weight(1f))
            }

            DrawerSectionTitle(appText(Res.string.drawer_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VendorChip(appText(Res.string.drawer_theme_system), themePreference == AppThemePreference.System, { onThemePreferenceChange(AppThemePreference.System) }, Modifier.weight(1f))
                VendorChip(appText(Res.string.drawer_theme_light), themePreference == AppThemePreference.Light, { onThemePreferenceChange(AppThemePreference.Light) }, Modifier.weight(1f))
                VendorChip(appText(Res.string.drawer_theme_dark), themePreference == AppThemePreference.Dark, { onThemePreferenceChange(AppThemePreference.Dark) }, Modifier.weight(1f))
            }

            DrawerActionRow(Icons.Filled.SupportAgent, appText(Res.string.drawer_support), detail = appText(Res.string.drawer_support_detail)) {}
            Spacer(Modifier.height(4.dp))
            DrawerActionRow(Icons.Filled.Logout, appText(Res.string.shell_sign_out), onClick = onLogout)
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable
private fun DrawerActionRow(icon: ImageVector, label: String, detail: String? = null, selected: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                detail?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun VendorSection.title(): String = when (this) {
    VendorSection.Dashboard -> appText(Res.string.nav_dashboard)
    VendorSection.Orders -> appText(Res.string.nav_orders)
    VendorSection.Catalog -> appText(Res.string.nav_catalog)
    VendorSection.Store -> appText(Res.string.nav_store)
    VendorSection.Notifications -> appText(Res.string.nav_notifications)
    VendorSection.Account -> appText(Res.string.nav_account)
}

@Composable
private fun VendorSection.subtitle(): String = when (this) {
    VendorSection.Dashboard -> appText(Res.string.nav_dashboard_subtitle)
    VendorSection.Orders -> appText(Res.string.nav_orders_subtitle)
    VendorSection.Catalog -> appText(Res.string.nav_catalog_subtitle)
    VendorSection.Store -> appText(Res.string.nav_store_subtitle)
    VendorSection.Notifications -> appText(Res.string.nav_notifications_subtitle)
    VendorSection.Account -> appText(Res.string.nav_account_subtitle)
}

private fun VendorSection.icon(): ImageVector = when (this) {
    VendorSection.Dashboard -> Icons.Filled.Home
    VendorSection.Orders -> Icons.Filled.ReceiptLong
    VendorSection.Catalog -> Icons.Filled.Inventory2
    VendorSection.Store -> Icons.Filled.Storefront
    VendorSection.Notifications -> Icons.Filled.Notifications
    VendorSection.Account -> Icons.Filled.Person
}

private val VendorDrawerSections = listOf(
    VendorSection.Dashboard,
    VendorSection.Orders,
    VendorSection.Catalog,
    VendorSection.Account,
)

private fun userFacingAuthError(error: Throwable, fallback: String, language: AppLanguage): String =
    when (error) {
        is AuthApiException -> appText(error.userMessage.stringResource(), language)
        else -> fallback
    }

private fun GoogleSignInFailureReason.stringResource(): org.jetbrains.compose.resources.StringResource =
    when (this) {
        GoogleSignInFailureReason.Failed -> Res.string.auth_error_google_failed
        GoogleSignInFailureReason.Interrupted -> Res.string.auth_error_google_interrupted
        GoogleSignInFailureReason.InProgress -> Res.string.auth_error_google_in_progress
        GoogleSignInFailureReason.NotConfigured -> Res.string.auth_error_google_not_configured
        GoogleSignInFailureReason.Unavailable -> Res.string.auth_error_google_unavailable
    }

private fun AuthUserMessage.stringResource(): org.jetbrains.compose.resources.StringResource =
    when (this) {
        AuthUserMessage.EmailFormatInvalid -> Res.string.auth_error_email_format
        AuthUserMessage.EmailPasswordIncorrect -> Res.string.auth_error_email_password
        AuthUserMessage.AccountUsesAnotherMethod -> Res.string.auth_error_account_other_method
        AuthUserMessage.TooManyAttempts -> Res.string.auth_error_too_many_attempts
        AuthUserMessage.AuthenticationUnavailable -> Res.string.auth_error_unavailable
        AuthUserMessage.SocialSignInFailed -> Res.string.auth_error_social_failed
        AuthUserMessage.SocialSignInUnavailable -> Res.string.auth_error_social_unavailable
        AuthUserMessage.GoogleSignInUnavailable -> Res.string.auth_error_google_unavailable
        AuthUserMessage.GoogleSignInTemporarilyUnavailable -> Res.string.auth_error_google_not_configured
        AuthUserMessage.GoogleSignInExpired -> Res.string.auth_error_google_expired
        AuthUserMessage.GoogleMissingEmail -> Res.string.auth_error_google_missing_email
        AuthUserMessage.GoogleEmailUnverified -> Res.string.auth_error_google_unverified_email
        AuthUserMessage.GoogleSignInCouldNotVerify -> Res.string.auth_error_google_could_not_verify
        AuthUserMessage.ProfileLoadFailed -> Res.string.auth_error_profile_load_failed
        AuthUserMessage.SessionRestoreFailed -> Res.string.auth_error_session_restore_failed
        AuthUserMessage.MerchantRoleRequired -> Res.string.auth_merchant_required
    }
