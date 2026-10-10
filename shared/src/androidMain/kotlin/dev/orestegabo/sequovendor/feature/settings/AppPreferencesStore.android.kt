package dev.orestegabo.sequovendor.feature.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppPreferencesStore(): AppPreferencesStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidAppPreferencesStore(context) }
}

private class AndroidAppPreferencesStore(context: Context) : AppPreferencesStore {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    override suspend fun getLanguage(): AppLanguage? =
        preferences.getString(LanguageKey, null)?.let { value ->
            runCatching { AppLanguage.valueOf(value) }.getOrNull()
        }

    override suspend fun saveLanguage(language: AppLanguage) {
        preferences.edit().putString(LanguageKey, language.name).apply()
    }

    override suspend fun getThemePreference(): AppThemePreference? =
        preferences.getString(ThemeKey, null)?.let { value ->
            runCatching { AppThemePreference.valueOf(value) }.getOrNull()
        }

    override suspend fun saveThemePreference(themePreference: AppThemePreference) {
        preferences.edit().putString(ThemeKey, themePreference.name).apply()
    }

    private companion object {
        const val PreferencesName = "sequo_vendor_preferences"
        const val LanguageKey = "language"
        const val ThemeKey = "theme"
    }
}
