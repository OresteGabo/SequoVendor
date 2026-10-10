package dev.orestegabo.sequovendor.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberAppPreferencesStore(): AppPreferencesStore =
    remember { IosAppPreferencesStore() }

private class IosAppPreferencesStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : AppPreferencesStore {
    override suspend fun getLanguage(): AppLanguage? =
        userDefaults.stringForKey(LanguageKey)?.let { value ->
            runCatching { AppLanguage.valueOf(value) }.getOrNull()
        }

    override suspend fun saveLanguage(language: AppLanguage) {
        userDefaults.setObject(language.name, forKey = LanguageKey)
    }

    override suspend fun getThemePreference(): AppThemePreference? =
        userDefaults.stringForKey(ThemeKey)?.let { value ->
            runCatching { AppThemePreference.valueOf(value) }.getOrNull()
        }

    override suspend fun saveThemePreference(themePreference: AppThemePreference) {
        userDefaults.setObject(themePreference.name, forKey = ThemeKey)
    }

    private companion object {
        const val LanguageKey = "language"
        const val ThemeKey = "theme"
    }
}
