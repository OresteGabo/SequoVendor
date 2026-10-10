package dev.orestegabo.sequovendor.core.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.util.UUID

@Composable
actual fun rememberAuthDeviceId(): String {
    val context = LocalContext.current.applicationContext
    val isPreview = LocalInspectionMode.current
    return remember(context, isPreview) {
        if (isPreview) {
            "preview-device-id"
        } else {
            val preferences = context.getSharedPreferences("sequo_auth_device", Context.MODE_PRIVATE)
            preferences.getString(AuthDeviceIdKey, null)?.takeIf { it.isNotBlank() }
                ?: "android-${UUID.randomUUID()}".also { generated ->
                    preferences.edit().putString(AuthDeviceIdKey, generated).apply()
                }
        }
    }
}

private const val AuthDeviceIdKey = "auth_device_id"
