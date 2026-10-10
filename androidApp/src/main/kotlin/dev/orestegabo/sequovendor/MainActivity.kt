package dev.orestegabo.sequovendor

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.orestegabo.sequovendor.core.auth.GoogleSignInFailureReason
import dev.orestegabo.sequovendor.core.auth.GoogleSignInResult
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.security.SecureRandom
import kotlin.coroutines.resume

private const val GoogleSignInTag = "SequoVendorGoogleSignIn"

class MainActivity : ComponentActivity() {
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val secureRandom = SecureRandom()
    private var legacyGoogleSignInContinuation: CancellableContinuation<GoogleSignInResult>? = null
    private val legacyGoogleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        completeLegacyGoogleSignIn(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                onGoogleSignIn = ::signInWithGoogle,
                onGoogleSignOut = {
                    runCatching {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    }.onFailure { error ->
                        Log.w(GoogleSignInTag, "Could not clear Google credential state.", error)
                    }
                },
            )
        }
    }

    private suspend fun signInWithGoogle(): GoogleSignInResult {
        val googleIdOption = GetSignInWithGoogleOption.Builder(
            getString(R.string.google_server_client_id),
        )
            .setNonce(generateGoogleSignInNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = this,
                request = request,
            )
            val credential = result.credential
            Log.d(GoogleSignInTag, "Credential Manager returned ${credential::class.simpleName} type=${credential.type}")

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                if (googleCredential.idToken.isBlank()) {
                    Log.w(GoogleSignInTag, "Google credential did not include an ID token.")
                    return GoogleSignInResult.Failure(GoogleSignInFailureReason.Unavailable)
                }
                Log.d(
                    GoogleSignInTag,
                    "Google ID token received for ${googleCredential.id}; length=${googleCredential.idToken.length}; ${googleCredential.idToken.safeGoogleTokenSummary()}",
                )
                GoogleSignInResult.Success(
                    idToken = googleCredential.idToken,
                    displayName = googleCredential.displayName,
                    email = googleCredential.id,
                    profilePictureUri = googleCredential.profilePictureUri?.toString(),
                )
            } else {
                Log.w(GoogleSignInTag, "Google returned unsupported credential type=${credential.type}.")
                GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
            }
        } catch (error: GetCredentialCancellationException) {
            Log.e(GoogleSignInTag, "Credential Manager cancelled Google sign-in after account selection.", error)
            if (error.message?.contains("Account reauth failed", ignoreCase = true) == true) {
                Log.w(GoogleSignInTag, "Retrying Google sign-in with Play Services fallback.")
                signInWithLegacyGoogleClient()
            } else {
                GoogleSignInResult.Failure(GoogleSignInFailureReason.Interrupted)
            }
        } catch (error: GoogleIdTokenParsingException) {
            Log.e(GoogleSignInTag, "Google returned an invalid ID token credential.", error)
            GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
        } catch (error: GetCredentialException) {
            Log.e(GoogleSignInTag, "Credential Manager failed.", error)
            GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
        } catch (error: IllegalArgumentException) {
            Log.e(GoogleSignInTag, "Google sign-in response was invalid.", error)
            GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
        } catch (error: Throwable) {
            Log.e(GoogleSignInTag, "Unexpected Google sign-in failure.", error)
            GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
        }
    }

    private suspend fun signInWithLegacyGoogleClient(): GoogleSignInResult =
        suspendCancellableCoroutine { continuation ->
            if (legacyGoogleSignInContinuation != null) {
                continuation.resume(GoogleSignInResult.Failure(GoogleSignInFailureReason.InProgress))
                return@suspendCancellableCoroutine
            }

            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

            continuation.invokeOnCancellation {
                if (legacyGoogleSignInContinuation === continuation) {
                    legacyGoogleSignInContinuation = null
                }
            }

            googleSignInClient.signOut().addOnCompleteListener {
                if (!continuation.isActive) return@addOnCompleteListener
                legacyGoogleSignInContinuation = continuation
                runCatching {
                    legacyGoogleSignInLauncher.launch(googleSignInClient.signInIntent)
                }.onFailure { error ->
                    if (legacyGoogleSignInContinuation === continuation) {
                        legacyGoogleSignInContinuation = null
                    }
                    Log.e(GoogleSignInTag, "Could not launch Play Services Google sign-in fallback.", error)
                    continuation.resume(GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed))
                }
            }
        }

    private fun completeLegacyGoogleSignIn(result: ActivityResult) {
        val continuation = legacyGoogleSignInContinuation ?: return
        legacyGoogleSignInContinuation = null
        if (!continuation.isActive) return

        val account = try {
            GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
        } catch (error: ApiException) {
            Log.e(GoogleSignInTag, "Play Services Google sign-in failed with status ${error.statusCode}.", error)
            continuation.resume(
                if (result.data == null) {
                    GoogleSignInResult.Cancelled
                } else {
                    GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed)
                },
            )
            return
        } catch (error: Throwable) {
            Log.e(GoogleSignInTag, "Play Services Google sign-in failed.", error)
            continuation.resume(GoogleSignInResult.Failure(GoogleSignInFailureReason.Failed))
            return
        }

        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            Log.w(GoogleSignInTag, "Play Services Google sign-in did not return an ID token.")
            continuation.resume(GoogleSignInResult.Failure(GoogleSignInFailureReason.Unavailable))
            return
        }

        Log.d(
            GoogleSignInTag,
            "Play Services Google ID token received for ${account.email}; length=${idToken.length}; ${idToken.safeGoogleTokenSummary()}",
        )
        continuation.resume(
            GoogleSignInResult.Success(
                idToken = idToken,
                displayName = account.displayName,
                email = account.email,
                profilePictureUri = account.photoUrl?.toString(),
            ),
        )
    }

    private fun generateGoogleSignInNonce(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun String.safeGoogleTokenSummary(): String =
        runCatching {
            val payload = split('.').getOrNull(1).orEmpty()
            val paddedPayload = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
            val json = JSONObject(String(Base64.decode(paddedPayload, Base64.URL_SAFE or Base64.NO_WRAP)))
            "aud=${json.optString("aud", "missing")} iss=${json.optString("iss", "missing")} " +
                "emailVerified=${json.opt("email_verified") ?: "missing"} subPresent=${json.optString("sub").isNotBlank()}"
        }.getOrDefault("claims=unreadable")
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
