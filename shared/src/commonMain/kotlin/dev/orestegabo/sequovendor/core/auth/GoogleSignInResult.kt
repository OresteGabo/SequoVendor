package dev.orestegabo.sequovendor.core.auth

sealed interface GoogleSignInResult {
    data class Success(
        val idToken: String,
        val displayName: String? = null,
        val email: String? = null,
        val profilePictureUri: String? = null,
    ) : GoogleSignInResult

    data object Cancelled : GoogleSignInResult

    data class Failure(
        val reason: GoogleSignInFailureReason,
    ) : GoogleSignInResult
}

enum class GoogleSignInFailureReason {
    Failed,
    Interrupted,
    InProgress,
    NotConfigured,
    Unavailable,
}
