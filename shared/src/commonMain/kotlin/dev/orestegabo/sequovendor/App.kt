package dev.orestegabo.sequovendor

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import dev.orestegabo.sequovendor.core.auth.GoogleSignInFailureReason
import dev.orestegabo.sequovendor.core.auth.GoogleSignInResult
import dev.orestegabo.sequovendor.feature.settings.AppLanguage
import dev.orestegabo.sequovendor.feature.settings.AppThemePreference
import dev.orestegabo.sequovendor.feature.settings.LocalAppLanguage
import dev.orestegabo.sequovendor.feature.settings.rememberAppPreferencesStore
import dev.orestegabo.sequovendor.feature.splash.SplashScreen
import dev.orestegabo.sequovendor.theme.SequoVendorTheme
import dev.orestegabo.sequovendor.ui.app.SequoVendorShell

@Composable
@Preview
fun App(
    onGoogleSignIn: suspend () -> GoogleSignInResult = {
        GoogleSignInResult.Failure(GoogleSignInFailureReason.NotConfigured)
    },
    onGoogleSignOut: suspend () -> Unit = {},
) {
    val preferencesStore = rememberAppPreferencesStore()
    val scope = rememberCoroutineScope()
    var language by rememberSaveable { mutableStateOf(AppLanguage.English) }
    var themePreference by rememberSaveable { mutableStateOf(AppThemePreference.System) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(preferencesStore) {
        preferencesStore.getLanguage()?.let { language = it }
        preferencesStore.getThemePreference()?.let { themePreference = it }
    }
    val darkTheme = when (themePreference) {
        AppThemePreference.System -> isSystemInDarkTheme()
        AppThemePreference.Light -> false
        AppThemePreference.Dark -> true
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        SequoVendorTheme(darkTheme = darkTheme) {
            if (showSplash) {
                SplashScreen(onTimeout = { showSplash = false })
                return@SequoVendorTheme
            }
            SequoVendorShell(
                language = language,
                onLanguageChange = {
                    language = it
                    scope.launch { preferencesStore.saveLanguage(it) }
                },
                themePreference = themePreference,
                onThemePreferenceChange = {
                    themePreference = it
                    scope.launch { preferencesStore.saveThemePreference(it) }
                },
                onGoogleSignIn = onGoogleSignIn,
                onGoogleSignOut = onGoogleSignOut,
            )
        }
    }
}
