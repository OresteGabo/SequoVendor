package dev.orestegabo.sequovendor.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import dev.orestegabo.sequovendor.core.auth.createVendorWorkspaceClient
import dev.orestegabo.sequovendor.core.auth.LocalNativeAccount
import dev.orestegabo.sequovendor.core.auth.workspaceKey
import dev.orestegabo.sequovendor.core.auth.AuthApiClient
import dev.orestegabo.sequovendor.core.activity.MerchantActivityApiClient
import dev.orestegabo.sequovendor.core.activity.MerchantActivitySnapshot
import dev.orestegabo.sequovendor.core.auth.AuthApiException
import dev.orestegabo.sequovendor.core.auth.AuthRepository
import dev.orestegabo.sequovendor.core.auth.authorizedMerchantIds
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
import dev.orestegabo.sequovendor.core.catalog.CatalogDraftState
import dev.orestegabo.sequovendor.core.catalog.FakeVendorCatalogRepository
import dev.orestegabo.sequovendor.core.catalog.NetworkVendorCatalogRepository
import dev.orestegabo.sequovendor.core.catalog.VendorCatalogRepository
import dev.orestegabo.sequovendor.core.fulfillment.FakeMerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentApiClient
import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.NetworkMerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.fulfillment.VendorOrderAction
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiHealthClient
import dev.orestegabo.sequovendor.core.network.ApiHealthStatus
import dev.orestegabo.sequovendor.core.negotiation.FakeVendorNegotiationRepository
import dev.orestegabo.sequovendor.core.negotiation.VendorNegotiationApiClient
import dev.orestegabo.sequovendor.core.negotiation.NetworkVendorNegotiationRepository
import dev.orestegabo.sequovendor.core.negotiation.VendorNegotiationRepository
import dev.orestegabo.sequovendor.core.notifications.FakeVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NetworkVendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.NotificationsApiClient
import dev.orestegabo.sequovendor.core.notifications.VendorNotificationsRepository
import dev.orestegabo.sequovendor.core.notifications.loadInboxSnapshot
import dev.orestegabo.sequovendor.core.store.createVendorStoreRepository
import dev.orestegabo.sequovendor.core.store.VendorStoreApiClient
import dev.orestegabo.sequovendor.model.StoreAvailabilityState
import dev.orestegabo.sequovendor.core.store.VendorStoreRepository
import dev.orestegabo.sequovendor.feature.auth.AuthScreen
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.CatalogProduct
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.DemoVendorData
import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.OpeningHoursDay
import dev.orestegabo.sequovendor.model.OpeningHoursException
import dev.orestegabo.sequovendor.model.VendorNotification
import dev.orestegabo.sequovendor.model.VendorNotificationPreference
import dev.orestegabo.sequovendor.model.VendorNotificationPreferenceEventType
import dev.orestegabo.sequovendor.model.VendorNegotiationThread
import dev.orestegabo.sequovendor.model.VendorSection
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.ui.account.AccountContent
import dev.orestegabo.sequovendor.ui.navigation.VendorBackHandler
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
import kotlinx.coroutines.CancellationException
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.store_sync_loading
import sequovendor.shared.generated.resources.store_sync_failed
import sequovendor.shared.generated.resources.store_refresh
import sequovendor.shared.generated.resources.store_retry_save
import sequovendor.shared.generated.resources.auth_local_test_accounts
import sequovendor.shared.generated.resources.auth_local_test_detail
import sequovendor.shared.generated.resources.auth_local_test_cancel
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
import sequovendor.shared.generated.resources.auth_choose_merchant
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
    var merchantChoice by remember { mutableStateOf<CurrentUser?>(null) }
    var authNotice by remember { mutableStateOf<String?>(null) }
    var emailAuthInProgress by remember { mutableStateOf(false) }
    var googleSignInInProgress by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf(VendorSection.Dashboard) }
    var apiHealthStatus by remember { mutableStateOf<ApiHealthStatus?>(null) }
    var localTestAccounts by remember { mutableStateOf(emptyList<LocalNativeAccount>()) }
    var showLocalTestAccounts by remember { mutableStateOf(false) }
    var localTestBusy by remember { mutableStateOf(false) }
    LaunchedEffect(authRepository) {
        runCatching { authRepository.localDevelopmentAccess() }.onSuccess { access ->
            localTestAccounts = if (access.enabled && access.mode == "LOCAL_FIXTURE") access.accounts else emptyList()
        }
    }

    val localOrders = remember { DemoVendorData.orders() }
    val localProducts = remember { DemoVendorData.products() }
    val localNotifications = remember { DemoVendorData.notifications() }
    val localNegotiations = remember { DemoVendorData.negotiations() }
    var products by remember(session?.userId, session?.merchantId, session?.localMode) { mutableStateOf(emptyList<CatalogProduct>()) }
    var notifications by remember(session?.userId, session?.merchantId, session?.localMode) {
        mutableStateOf(if (session?.localMode == true) localNotifications else emptyList<VendorNotification>())
    }
    var negotiations by remember(session?.userId, session?.merchantId, session?.localMode) { mutableStateOf(emptyList<VendorNegotiationThread>()) }
    var notificationPreference by remember(session?.userId, session?.merchantId, session?.localMode) { mutableStateOf(VendorNotificationPreference()) }
    suspend fun clearSavedSessionForDifferentAccount() {
        merchantChoice = null
        if (rememberedUser != null || authRepository.getSavedSession() != null) {
            runCatching { authRepository.logout() }
            runCatching { onGoogleSignOut() }
            rememberedUser = null
            session = null
        }
    }

    suspend fun openMerchantWorkspace(user: CurrentUser, selection: String? = null) {
        val authorizedIds = user.authorizedMerchantIds()
        if (authorizedIds.isEmpty() || (selection != null && selection !in authorizedIds)) {
            authRepository.logout()
            rememberedUser = null
            merchantChoice = null
            authNotice = authError(AuthApiException(403, AuthUserMessage.MerchantRoleRequired))
            return
        }
        val merchantId = selection ?: authorizedIds.singleOrNull()
        if (merchantId == null) {
            merchantChoice = user
            return
        }
        val saved = authRepository.getSavedSession() ?: return
        rememberedUser = user
        authRepository.rememberUser(user)
        merchantChoice = null
        session = VendorSession(
            merchantId = merchantId,
            displayName = user.displayName,
            accessToken = saved.accessToken,
            authProvider = user.provider,
            localMode = false,
            userId = user.id,
            email = user.email,
            status = user.status,
        )
    }

    LaunchedEffect(authRepository) {
        try {
            rememberedUser = authRepository.getRememberedUser()
            authRepository.restoreSavedSession()?.let { openMerchantWorkspace(it) }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { authNotice = authError(failure) }
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
                    runCatching {
                        val user = authRepository.restoreSavedSession()
                        if (user != null) openMerchantWorkspace(user) else authNotice = notReadyMessage
                    }.onFailure { authNotice = authError(it) }
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
                        if (user != null) openMerchantWorkspace(user) else authNotice = notReadyMessage
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
                        if (user != null) openMerchantWorkspace(user) else authNotice = notReadyMessage
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
                                if (user != null) openMerchantWorkspace(user) else authNotice = notReadyMessage
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
            onLocalTestAccountsClick = if (localTestAccounts.isEmpty()) null else ({ authNotice = null; showLocalTestAccounts = true }),
            localTestBusy = localTestBusy,
            modifier = Modifier.fillMaxSize(),
        )
        if (showLocalTestAccounts && localTestAccounts.isNotEmpty()) AlertDialog(
            onDismissRequest = { if (!localTestBusy) showLocalTestAccounts = false },
            title = { Text(appText(Res.string.auth_local_test_accounts)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(appText(Res.string.auth_local_test_detail))
                authNotice?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                localTestAccounts.forEach { account ->
                    TextButton(enabled = !localTestBusy && !emailAuthInProgress && !googleSignInInProgress, onClick = {
                        if (!localTestBusy) {
                            localTestBusy = true
                            authNotice = null
                            scope.launch {
                                try {
                                    clearSavedSessionForDifferentAccount()
                                    val verified = authRepository.loginWithDevelopmentAccount(account.key)
                                    showLocalTestAccounts = false
                                    openMerchantWorkspace(verified)
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (failure: Exception) { authNotice = authError(failure) }
                                finally { localTestBusy = false }
                            }
                        }
                    }) { Text(account.label) }
                }
            } },
            confirmButton = { TextButton(enabled = !localTestBusy, onClick = { showLocalTestAccounts = false }) { Text(appText(Res.string.auth_local_test_cancel)) } },
        )
        merchantChoice?.let { user ->
            AlertDialog(
                onDismissRequest = { merchantChoice = null },
                title = { Text(appText(Res.string.auth_choose_merchant, language)) },
                text = {
                    Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                        user.authorizedMerchantIds().sorted().forEach { merchantId ->
                            TextButton(onClick = {
                                scope.launch {
                                    runCatching {
                                        val verified = authRepository.restoreSavedSession()
                                        if (verified != null && verified.id == user.id) {
                                            openMerchantWorkspace(verified, merchantId)
                                        } else {
                                            merchantChoice = null
                                            authNotice = notReadyMessage
                                        }
                                    }.onFailure { authNotice = authError(it) }
                                }
                            }) { Text(merchantId) }
                        }
                    }
                },
                confirmButton = {},
            )
        }
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
    val workspaceKey = activeSession.workspaceKey()
    fun unavailableStore() = StoreAvailabilityState(merchantId = activeSession.merchantId, open = false,
        weekdayHours = "", weekendHours = "", configured = false, openStatus = "UNAVAILABLE")
    var store by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) {
        mutableStateOf(if (activeSession.localMode) DemoVendorData.store() else unavailableStore())
    }
    var closureNote by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf("") }
    var storeBusy by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var storeFailure by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<ApiFailure?>(null) }
    var storePendingRetry by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    val storeChangesEnabled = !storeBusy && store.openStatus != "UNAVAILABLE"
    var dashboardOrders by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) {
        mutableStateOf(if (activeSession.localMode) localOrders else emptyList<MerchantSubOrder>())
    }
    var dashboardConnectionState by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) {
        mutableStateOf(if (activeSession.localMode) DashboardConnectionState.LocalFallback else DashboardConnectionState.Connected)
    }
    var ordersLoading by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var orderActionInProgressId by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<String?>(null) }
    var orderActionFailed by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var detailOrder by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<MerchantSubOrder?>(null) }
    var detailSla by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<MerchantSlaInfo?>(null) }
    var detailLoading by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var detailOffline by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var catalogLoading by remember(workspaceKey) { mutableStateOf(false) }
    var catalogOffline by remember(workspaceKey) { mutableStateOf(false) }
    var catalogSaveInProgress by remember(workspaceKey) { mutableStateOf(false) }
    var catalogSaveError by remember(workspaceKey) { mutableStateOf<ApiFailure?>(null) }
    var lastCatalogSaveState by remember(workspaceKey) { mutableStateOf<CatalogDraftState?>(null) }
    var notificationsLoading by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var notificationsOffline by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var notificationActionFailed by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var notificationPreferenceSaved by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var includeArchivedNotifications by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var negotiationActionInProgressId by remember(activeSession.userId, activeSession.merchantId) { mutableStateOf<String?>(null) }
    var negotiationFailure by remember(activeSession.userId, activeSession.merchantId) { mutableStateOf<ApiFailure?>(null) }
    var negotiationsLoading by remember(activeSession.userId, activeSession.merchantId) { mutableStateOf(false) }
    var negotiationsStale by remember(activeSession.userId, activeSession.merchantId) { mutableStateOf(true) }
    var activitySnapshot by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<MerchantActivitySnapshot?>(null) }
    var activityLoading by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(false) }
    var activityFailure by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf<ApiFailure?>(null) }
    var activityRefresh by remember(activeSession.userId, activeSession.merchantId, activeSession.localMode) { mutableStateOf(0) }
    fun sameMerchantSession(): Boolean = session?.workspaceKey() == workspaceKey
    val workspaceClient = remember(workspaceKey) {
        if (activeSession.localMode) null else createVendorWorkspaceClient(
            expected = workspaceKey,
            currentSession = { session },
            restoreUser = { authRepository.restoreSavedSession() },
            savedAccessToken = { authRepository.getSavedSession()?.accessToken },
            onRefreshed = { token -> if (sameMerchantSession()) session = session?.copy(accessToken = token) },
        )
    }
    DisposableEffect(workspaceClient) { onDispose { workspaceClient?.close() } }
    val activityClient = remember(workspaceKey) {
        workspaceClient?.let { MerchantActivityApiClient(httpClient = it) }
    }
    val storeRepository: VendorStoreRepository = remember(workspaceKey) {
        createVendorStoreRepository(activeSession.localMode, store) {
            VendorStoreApiClient(requireNotNull(workspaceClient))
        }
    }
    suspend fun updateStore(block: suspend () -> ApiResult<StoreAvailabilityState>) {
        if (storeBusy || !sameMerchantSession()) return
        storeBusy = true; storeFailure = null
        try {
            val result = block()
            if (!sameMerchantSession()) return
            storePendingRetry = storeRepository.hasPending(activeSession.merchantId)
            when (result) {
                is ApiResult.Success -> { store = result.value; closureNote = result.value.closureReason ?: result.value.temporaryClosure?.reason.orEmpty() }
                is ApiResult.Failure -> { store = unavailableStore(); storeFailure = result.error }
            }
        } finally { if (sameMerchantSession()) storeBusy = false }
    }
    LaunchedEffect(storeRepository) { updateStore { storeRepository.getAvailability(activeSession.merchantId) } }

    val negotiationRepository: VendorNegotiationRepository = remember(workspaceKey) {
        if (activeSession.localMode) FakeVendorNegotiationRepository(localNegotiations)
        else NetworkVendorNegotiationRepository(VendorNegotiationApiClient(httpClient = requireNotNull(workspaceClient)), activeSession.merchantId)
    }
    val catalogRepository: VendorCatalogRepository = remember(workspaceKey) {
        if (activeSession.localMode) FakeVendorCatalogRepository(localProducts)
        else NetworkVendorCatalogRepository(CatalogApiClient(httpClient = requireNotNull(workspaceClient)))
    }
    val fulfillmentRepository: MerchantFulfillmentRepository = remember(workspaceKey) {
        if (activeSession.localMode) FakeMerchantFulfillmentRepository(localOrders)
        else NetworkMerchantFulfillmentRepository(MerchantFulfillmentApiClient(httpClient = requireNotNull(workspaceClient)))
    }
    val notificationsRepository: VendorNotificationsRepository = remember(workspaceKey) {
        if (activeSession.localMode) FakeVendorNotificationsRepository(localNotifications)
        else NetworkVendorNotificationsRepository(NotificationsApiClient(httpClient = requireNotNull(workspaceClient)))
    }
    val setStoreOpen: (Boolean) -> Unit = { open ->
        if (storeChangesEnabled) scope.launch { updateStore { storeRepository.setOpen(activeSession.merchantId, open, closureNote) } }
    }
    fun checkApiHealth() { scope.launch { apiHealthStatus = apiHealthClient.checkHealth() } }
    val setClosureReason: (String) -> Unit = { note -> closureNote = note }
    val saveClosureReason: () -> Unit = {
        val note = closureNote
        if (storeChangesEnabled) scope.launch { updateStore { storeRepository.setTemporaryClosureReason(activeSession.merchantId, note) } }
    }
    val setOpeningHours: (List<OpeningHoursDay>) -> Unit = { hours ->
        if (storeChangesEnabled) scope.launch { updateStore { storeRepository.setOpeningHours(activeSession.merchantId, hours) } }
    }
    val addTemporaryException: (OpeningHoursException) -> Unit = { exception ->
        if (storeChangesEnabled) scope.launch { updateStore { storeRepository.addTemporaryException(activeSession.merchantId, exception) } }
    }

    suspend fun refreshMerchantOrders(showLoading: Boolean = true) {
        if (showLoading) ordersLoading = true
        if (!activeSession.localMode) dashboardOrders = emptyList()
        try {
            val result = fulfillmentRepository.listSubOrders(activeSession.merchantId)
            if (!sameMerchantSession()) return
            when (result) {
                is ApiResult.Success -> {
                    dashboardOrders = result.value
                    dashboardConnectionState = if (activeSession.localMode) DashboardConnectionState.LocalFallback else DashboardConnectionState.Connected
                }
                is ApiResult.Failure -> {
                    dashboardOrders = emptyList()
                    detailOrder = null
                    detailSla = null
                    activitySnapshot = null
                    activityFailure = result.error
                    dashboardConnectionState = DashboardConnectionState.ErrorFallback
                }
            }
        } finally { if (sameMerchantSession()) ordersLoading = false }
    }

    suspend fun refreshCatalog(showLoading: Boolean = true) {
        if (showLoading) catalogLoading = true
        try {
            val result = catalogRepository.listProducts(activeSession.merchantId)
            if (!sameMerchantSession()) return
            when (result) {
                is ApiResult.Success -> { products = result.value; catalogOffline = false }
                is ApiResult.Failure -> catalogOffline = true
            }
        } finally { if (sameMerchantSession()) catalogLoading = false }
    }

    suspend fun refreshNotifications(showLoading: Boolean = true) {
        if (!sameMerchantSession()) return
        val requestedIncludeArchived = includeArchivedNotifications
        fun currentRequest() = sameMerchantSession() && requestedIncludeArchived == includeArchivedNotifications
        if (showLoading) notificationsLoading = true
        notificationActionFailed = false
        try {
            val snapshot = notificationsRepository.loadInboxSnapshot(requestedIncludeArchived)
            if (!currentRequest()) return
            notifications = snapshot.notifications
            notificationsOffline = snapshot.isOffline
            when (val preferenceResult = notificationsRepository.effectivePreference(VendorNotificationPreferenceEventType.ORDER_CREATED)) {
                is ApiResult.Success -> if (currentRequest()) {
                    notificationPreference = preferenceResult.value.copy(eventType = VendorNotificationPreferenceEventType.ALL)
                }
                is ApiResult.Failure -> Unit
            }
        } finally {
            if (currentRequest()) notificationsLoading = false
        }
    }

    fun replaceNotification(updated: VendorNotification) {
        notifications = notifications.map { if (it.id == updated.id) updated else it }
    }

    fun handleNotificationAction(
        notification: VendorNotification,
        action: suspend (String) -> ApiResult<VendorNotification>,
    ) {
        scope.launch {
            if (!sameMerchantSession()) return@launch
            notificationActionFailed = false
            notificationPreferenceSaved = false
            val result = action(notification.id)
            if (!sameMerchantSession()) return@launch
            when (result) {
                is ApiResult.Success -> replaceNotification(result.value)
                is ApiResult.Failure -> notificationActionFailed = true
            }
        }
    }

    fun saveNotificationPreference(preference: VendorNotificationPreference) {
        scope.launch {
            if (!sameMerchantSession()) return@launch
            notificationActionFailed = false
            notificationPreferenceSaved = false
            val result = notificationsRepository.savePreference(preference)
            if (!sameMerchantSession()) return@launch
            when (result) {
                is ApiResult.Success -> {
                    notificationPreference = result.value
                    notificationPreferenceSaved = true
                }
                is ApiResult.Failure -> notificationActionFailed = true
            }
        }
    }

    fun sameNegotiationSession(): Boolean = session?.userId == activeSession.userId &&
        session?.merchantId == activeSession.merchantId && session?.localMode == activeSession.localMode

    suspend fun refreshNegotiations() {
        if (negotiationsLoading || negotiationActionInProgressId != null) return
        negotiationsLoading = true
        negotiationFailure = null
        when (val result = negotiationRepository.listThreads(activeSession.merchantId)) {
            is ApiResult.Success -> if (sameNegotiationSession()) {
                negotiations = result.value
                negotiationsStale = false
            }
            is ApiResult.Failure -> if (sameNegotiationSession()) {
                negotiationFailure = result.error
                negotiationsStale = true
            }
        }
        if (sameNegotiationSession()) negotiationsLoading = false
    }

    fun handleNegotiationAction(thread: VendorNegotiationThread, action: suspend () -> ApiResult<VendorNegotiationThread>) {
        if (negotiationActionInProgressId != null || negotiationsLoading || negotiationsStale) return
        // Mark busy before launch so repeated taps cannot launch parallel mutations.
        negotiationActionInProgressId = thread.id
        negotiationFailure = null
        scope.launch {
            try {
                when (val result = action()) {
                    is ApiResult.Success -> if (sameNegotiationSession()) {
                        negotiations = negotiations.map { if (it.id == result.value.id) result.value else it }
                    }
                    is ApiResult.Failure -> if (sameNegotiationSession()) {
                        negotiationFailure = result.error
                        negotiationsStale = true
                    }
                }
            } finally {
                if (sameNegotiationSession()) negotiationActionInProgressId = null
            }
        }
    }

    fun saveProduct(product: CatalogProduct) {
        if (catalogSaveInProgress || !sameMerchantSession()) return
        catalogSaveInProgress = true
        scope.launch {
            lastCatalogSaveState = null
            catalogSaveError = null
            try {
                val result = catalogRepository.saveProduct(activeSession.merchantId, product)
                if (!sameMerchantSession()) return@launch
                when (result) {
                    is ApiResult.Success -> {
                        products = products.filterNot { it.id == product.id || it.id == result.value.product.id } + result.value.product
                        lastCatalogSaveState = result.value.state
                    }
                    is ApiResult.Failure -> catalogSaveError = result.error
                }
            } finally { if (sameMerchantSession()) catalogSaveInProgress = false }
        }
    }

    fun saveProductOptions(product: CatalogProduct, groups: List<CatalogFoodOptionGroup>) {
        val repository = catalogRepository as? NetworkVendorCatalogRepository ?: return
        if (catalogSaveInProgress || !sameMerchantSession()) return
        catalogSaveInProgress = true
        scope.launch {
            catalogSaveError = null
            try {
                val result = repository.saveOptions(activeSession.merchantId, product.id, groups)
                if (!sameMerchantSession()) return@launch
                when (result) {
                    is ApiResult.Success -> products = products.map {
                        if (it.id == product.id) it.copy(foodOptionGroups = result.value) else it
                    }
                    is ApiResult.Failure -> catalogSaveError = result.error
                }
            } finally {
                if (sameMerchantSession()) catalogSaveInProgress = false
            }
        }
    }

    fun attachProductPhoto(product: CatalogProduct, jpeg: ByteArray?) {
        val repository = catalogRepository as? NetworkVendorCatalogRepository ?: return
        if (catalogSaveInProgress || !sameMerchantSession()) return
        catalogSaveInProgress = true
        scope.launch {
            catalogSaveError = null
            try {
                val result = repository.attachPhoto(activeSession.merchantId, product, jpeg, deviceId)
                if (!sameMerchantSession()) return@launch
                when (result) {
                    is ApiResult.Success -> products = products.map {
                        if (it.id == result.value.id) result.value else it
                    }
                    is ApiResult.Failure -> catalogSaveError = result.error
                }
            } finally {
                if (sameMerchantSession()) catalogSaveInProgress = false
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
        detailOrder = if (activeSession.localMode) fallback else null
        detailSla = null

        val orderResult = fulfillmentRepository.getSubOrder(subOrderId, activeSession.merchantId)
        if (!sameMerchantSession()) return
        when (orderResult) {
            is ApiResult.Success -> {
                detailOrder = orderResult.value
                dashboardConnectionState = if (activeSession.localMode) {
                    DashboardConnectionState.LocalFallback
                } else {
                    DashboardConnectionState.Connected
                }
                replaceMerchantOrder(orderResult.value)
                val slaResult = fulfillmentRepository.getSla(subOrderId, activeSession.merchantId)
                if (!sameMerchantSession()) return
                detailSla = when (slaResult) {
                    is ApiResult.Success -> slaResult.value
                    is ApiResult.Failure -> orderResult.value.sla
                }
            }
            is ApiResult.Failure -> {
                detailOffline = true
                detailOrder = null
                detailSla = null
                dashboardOrders = emptyList()
                activitySnapshot = null
                activityFailure = orderResult.error
                dashboardConnectionState = DashboardConnectionState.ErrorFallback
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
        if (!activeSession.localMode &&
            (dashboardConnectionState == DashboardConnectionState.ErrorFallback || detailOffline)
        ) {
            orderActionFailed = true
            return
        }
        orderActionInProgressId = order.id
        scope.launch {
            try {
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
                if (!sameMerchantSession()) return@launch
                when (result) {
                    is ApiResult.Success -> {
                        activitySnapshot = null
                        activityRefresh++
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
                        dashboardOrders = emptyList()
                        detailOrder = null
                        detailSla = null
                        activitySnapshot = null
                        activityFailure = result.error
                        dashboardConnectionState = DashboardConnectionState.ErrorFallback
                    }
                }
            } finally {
                orderActionInProgressId = null
            }
        }
    }

    LaunchedEffect(activityClient, activityRefresh) {
        activitySnapshot = null
        activityFailure = null
        activityLoading = activityClient != null
        try {
            val result = activityClient?.load(activeSession.merchantId) ?: return@LaunchedEffect
            if (!sameMerchantSession() || activityFailure != null) return@LaunchedEffect
            when (result) {
                is ApiResult.Success -> activitySnapshot = result.value
                is ApiResult.Failure -> activityFailure = result.error
            }
        } finally { if (sameMerchantSession()) activityLoading = false }
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

    LaunchedEffect(negotiationRepository, currentSection) {
        if (currentSection == VendorSection.Account) refreshNegotiations()
    }

    LaunchedEffect(Unit) {
        checkApiHealth()
    }

    // Registered before child editors/dialogs so their close actions take precedence.
    VendorBackHandler(enabled = currentSection != VendorSection.Dashboard) {
        if (currentSection == VendorSection.Orders && detailOrder != null) {
            detailOrder = null
            detailSla = null
            detailOffline = false
        } else {
            currentSection = VendorSection.Dashboard
        }
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
                if (currentSection in listOf(VendorSection.Dashboard, VendorSection.Store, VendorSection.Account)) {
                    if (storeBusy) Text(appText(Res.string.store_sync_loading))
                    storeFailure?.let { Text(appText(Res.string.store_sync_failed), color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(enabled = !storeBusy, onClick = { scope.launch { updateStore { storeRepository.getAvailability(activeSession.merchantId) } } }) { Text(appText(Res.string.store_refresh)) }
                        if (storePendingRetry) TextButton(enabled = !storeBusy, onClick = { scope.launch { updateStore { storeRepository.retryPending(activeSession.merchantId) } } }) { Text(appText(Res.string.store_retry_save)) }
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    when (currentSection) {
                        VendorSection.Dashboard -> DashboardContent(
                            session = activeSession,
                            orders = dashboardOrders,
                            products = products,
                            store = store,
                            storeChangesEnabled = storeChangesEnabled,
                            connectionState = dashboardConnectionState,
                            activity = activitySnapshot,
                            activityLoading = activityLoading,
                            activityFailure = activityFailure,
                            onRefreshActivity = { activitySnapshot = null; activityRefresh++ },
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
                            onRefresh = {
                                scope.launch {
                                    refreshMerchantOrders(showLoading = true)
                                    detailOrder?.let { loadOrderDetail(it.id, fallback = it) }
                                }
                            },
                            onOrderAction = ::handleOrderAction,
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Catalog -> CatalogContent(
                            products = products,
                            isLoading = catalogLoading,
                            isOffline = catalogOffline,
                            onRefresh = { scope.launch { refreshCatalog(showLoading = true) } },
                            onSaveProduct = ::saveProduct,
                            onSaveOptions = if (activeSession.localMode) null else ::saveProductOptions,
                            onAttachPhoto = if (activeSession.localMode) null else ::attachProductPhoto,
                            saveInProgress = catalogSaveInProgress,
                            lastSaveState = lastCatalogSaveState,
                            saveError = catalogSaveError,
                            onClearSaveResult = {
                                catalogSaveError = null
                                lastCatalogSaveState = null
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                        VendorSection.Store -> StoreContent(
                            status = store,
                            closureReason = closureNote,
                            onOpenChange = setStoreOpen,
                            onClosureReasonChange = setClosureReason,
                            onSaveClosureReason = saveClosureReason,
                            changesEnabled = storeChangesEnabled,
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
                            storeChangesEnabled = storeChangesEnabled,
                            language = language,
                            onLanguageChange = onLanguageChange,
                            themePreference = themePreference,
                            onThemePreferenceChange = onThemePreferenceChange,
                            apiHealthStatus = apiHealthStatus,
                            onCheckApiHealth = ::checkApiHealth,
                            onAvailabilityChange = setStoreOpen,
                            onStoreClick = { currentSection = VendorSection.Store },
                            onNotificationsClick = { currentSection = VendorSection.Notifications },
                            negotiations = negotiations,
                            negotiationActionInProgressId = negotiationActionInProgressId,
                            negotiationFailure = negotiationFailure,
                            negotiationsLoading = negotiationsLoading,
                            negotiationsStale = negotiationsStale,
                            onRefreshNegotiations = { scope.launch { refreshNegotiations() } },
                            onAcceptNegotiation = { thread -> handleNegotiationAction(thread) { negotiationRepository.accept(thread.id) } },
                            onRejectNegotiation = { thread, reason -> handleNegotiationAction(thread) { negotiationRepository.reject(thread.id, reason) } },
                            onCounterNegotiation = { thread, amount -> handleNegotiationAction(thread) { negotiationRepository.counter(thread.id, amount) } },
                            onMessageNegotiation = { thread, message -> handleNegotiationAction(thread) { negotiationRepository.message(thread.id, message) } },
                            onFinancialRecordsClick = { currentSection = VendorSection.Dashboard },
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
    // The open drawer closes before a child editor or the selected tab.
    VendorBackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
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
