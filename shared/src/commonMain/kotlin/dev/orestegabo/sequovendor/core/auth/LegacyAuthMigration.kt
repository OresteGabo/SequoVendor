package dev.orestegabo.sequovendor.core.auth

/** Callers serialize access; a failed secure write or readback always retains the legacy value. */
internal suspend fun readMigratingAuthValue(
    readSecure: suspend () -> String?,
    readLegacy: () -> String?,
    writeSecure: suspend (String) -> Unit,
    removeLegacy: () -> Unit,
    valid: (String) -> Boolean,
): String? {
    val secure = readSecure()
    if (secure != null) {
        check(valid(secure)) { "Secure session storage is unavailable. Please try again." }
        if (readLegacy() != null) saveMigratedAuthValue(secure, writeSecure, readSecure, removeLegacy)
        return secure
    }
    val legacy = readLegacy() ?: return null
    if (!valid(legacy)) return null
    saveMigratedAuthValue(legacy, writeSecure, readSecure, removeLegacy)
    return legacy
}

internal suspend fun saveMigratedAuthValue(
    value: String,
    writeSecure: suspend (String) -> Unit,
    readSecure: suspend () -> String?,
    removeLegacy: () -> Unit,
) {
    writeSecure(value)
    check(readSecure() == value) { "Secure session storage is unavailable. Please try again." }
    removeLegacy()
}
