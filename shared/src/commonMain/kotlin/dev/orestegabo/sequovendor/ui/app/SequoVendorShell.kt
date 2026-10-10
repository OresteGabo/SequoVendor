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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import dev.orestegabo.sequovendor.feature.auth.AuthScreen
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.DemoVendorData
import dev.orestegabo.sequovendor.model.VendorSection
import dev.orestegabo.sequovendor.model.VendorSession
import dev.orestegabo.sequovendor.ui.catalog.CatalogContent
import dev.orestegabo.sequovendor.ui.components.MetricTile
import dev.orestegabo.sequovendor.ui.components.VendorCard
import dev.orestegabo.sequovendor.ui.components.VendorChip
import dev.orestegabo.sequovendor.ui.components.VendorMark
import dev.orestegabo.sequovendor.ui.chrome.VendorBottomBar
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
import sequovendor.shared.generated.resources.nav_orders
import sequovendor.shared.generated.resources.nav_orders_subtitle
import sequovendor.shared.generated.resources.nav_store
import sequovendor.shared.generated.resources.nav_store_subtitle
import sequovendor.shared.generated.resources.shell_local_mode
import sequovendor.shared.generated.resources.shell_sign_out
import sequovendor.shared.generated.resources.state_off
import sequovendor.shared.generated.resources.state_on

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
    var currentSection by remember { mutableStateOf(VendorSection.Orders) }

    val orders = remember { DemoVendorData.orders() }
    val products = remember { DemoVendorData.products() }
    val store = remember { DemoVendorData.store() }

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
                        localMode = false,
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
                            session = VendorSession(user.id, user.displayName, saved.accessToken, localMode = false)
                        }
                    } ?: run { authNotice = notReadyMessage }
                }
            },
            onEmailLogin = { email, password ->
                scope.launch {
                    emailAuthInProgress = true
                    runCatching {
                        authRepository.loginWithEmail(email, password)
                        authRepository.currentUser()
                    }.onSuccess { user ->
                        if (user != null) {
                            rememberedUser = user
                            authRepository.rememberUser(user)
                            session = VendorSession(user.id, user.displayName, authRepository.getSavedSession()?.accessToken, localMode = false)
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
                        authRepository.signUpWithEmail(email, password, name)
                        authRepository.currentUser()
                    }.onSuccess { user ->
                        if (user != null) {
                            rememberedUser = user
                            authRepository.rememberUser(user)
                            session = VendorSession(user.id, user.displayName, authRepository.getSavedSession()?.accessToken, localMode = false)
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
                                    session = VendorSession(user.id, user.displayName, authRepository.getSavedSession()?.accessToken, localMode = false)
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
            onAppleLogin = { authNotice = notReadyMessage },
            onFacebookLogin = { authNotice = notReadyMessage },
            onWhatsAppLogin = { authNotice = notReadyMessage },
            onPasskeyLogin = { authNotice = notReadyMessage },
            onSequoLogin = { authNotice = notReadyMessage },
            onSkipAuth = { session = DemoVendorData.session() },
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
                MerchantSummary(activeSession.displayName, activeSession.merchantId, activeSession.localMode, orders.size, products.size)
                Box(Modifier.fillMaxSize()) {
                    when (currentSection) {
                        VendorSection.Orders -> OrdersContent(orders, Modifier.fillMaxSize())
                        VendorSection.Catalog -> CatalogContent(products, Modifier.fillMaxSize())
                        VendorSection.Store -> StoreContent(store, Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorTopAppBar(section: VendorSection, onMenuClick: () -> Unit) {
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
            IconButton(onClick = {}) {
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
            VendorSection.entries.forEach { section ->
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
private fun MerchantSummary(name: String, merchantId: String, localMode: Boolean, orders: Int, products: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(merchantId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(appText(Res.string.nav_orders), orders.toString(), Modifier.weight(1f))
            MetricTile(appText(Res.string.nav_catalog), products.toString(), Modifier.weight(1f))
            MetricTile(appText(Res.string.shell_local_mode), if (localMode) appText(Res.string.state_on) else appText(Res.string.state_off), Modifier.weight(1f))
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
    VendorSection.Orders -> appText(Res.string.nav_orders)
    VendorSection.Catalog -> appText(Res.string.nav_catalog)
    VendorSection.Store -> appText(Res.string.nav_store)
}

@Composable
private fun VendorSection.subtitle(): String = when (this) {
    VendorSection.Orders -> appText(Res.string.nav_orders_subtitle)
    VendorSection.Catalog -> appText(Res.string.nav_catalog_subtitle)
    VendorSection.Store -> appText(Res.string.nav_store_subtitle)
}

private fun VendorSection.icon(): ImageVector = when (this) {
    VendorSection.Orders -> Icons.Filled.ReceiptLong
    VendorSection.Catalog -> Icons.Filled.Inventory2
    VendorSection.Store -> Icons.Filled.Storefront
}

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
