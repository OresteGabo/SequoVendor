package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberAuthDeviceId(): String =
    remember {
        val userDefaults = NSUserDefaults.standardUserDefaults
        userDefaults.stringForKey(AuthDeviceIdKey)?.takeIf { it.isNotBlank() }
            ?: "ios-${NSUUID().UUIDString}".also { generated ->
                userDefaults.setObject(generated, forKey = AuthDeviceIdKey)
            }
    }

private const val AuthDeviceIdKey = "auth_device_id"
