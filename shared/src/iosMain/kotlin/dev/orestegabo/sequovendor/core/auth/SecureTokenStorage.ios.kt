package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@Composable
actual fun rememberSecureTokenStorage(): SecureTokenStorage =
    remember { IosSecureTokenStorage() }

@OptIn(ExperimentalForeignApi::class)
private class IosSecureTokenStorage : SecureTokenStorage {
    override suspend fun saveRefreshToken(token: String) {
        val tokenData = token.toNSData() ?: return
        baseQueryMap().useCFDictionary { query ->
            SecItemDelete(query)
        }

        val status = (baseQueryMap() + mapOf<Any?, Any?>(
                kSecValueData to tokenData,
            )).useCFDictionary { query ->
                SecItemAdd(query, null)
            }
        if (status != errSecSuccess) {
            println("Keychain failed to save refresh token: $status")
        }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun getRefreshToken(): String? =
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = (baseQueryMap() + mapOf<Any?, Any?>(
                    kSecReturnData to true,
                    kSecMatchLimit to kSecMatchLimitOne,
                )).useCFDictionary { query ->
                    SecItemCopyMatching(query, result.ptr)
                }
            when (status) {
                errSecSuccess -> {
                    val data = result.value as? NSData
                    data?.toUtf8String()
                }
                errSecItemNotFound -> null
                else -> null
            }.also {
                result.value?.let { value -> CFRelease(value) }
            }
        }

    override suspend fun clearSession() {
        baseQueryMap().useCFDictionary { query ->
            SecItemDelete(query)
        }
    }

    private fun baseQueryMap(): Map<Any?, Any?> =
        mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to KeychainService,
            kSecAttrAccount to RefreshTokenAccount,
        )

    private companion object {
        const val KeychainService = "dev.orestegabo.sequovendor.auth"
        const val RefreshTokenAccount = "refresh_token"
    }
}

@OptIn(ExperimentalForeignApi::class)
private inline fun <T> Map<Any?, Any?>.useCFDictionary(block: (CFDictionaryRef) -> T): T {
    val dictionary = toRetainedCFDictionary()
    return try {
        block(dictionary)
    } finally {
        CFRelease(dictionary)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun Map<Any?, Any?>.toRetainedCFDictionary(): CFDictionaryRef {
    val dictionary = NSMutableDictionary()
    forEach { (key, value) ->
        if (key != null && value != null) {
            dictionary.setObject(value, forKey = key as platform.Foundation.NSCopyingProtocol)
        }
    }
    return CFBridgingRetain(dictionary) as CFDictionaryRef
}

@OptIn(BetaInteropApi::class)
private fun String.toNSData(): NSData? =
    NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)

@Suppress("CAST_NEVER_SUCCEEDS")
@OptIn(BetaInteropApi::class)
private fun NSData.toUtf8String(): String? =
    NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()
