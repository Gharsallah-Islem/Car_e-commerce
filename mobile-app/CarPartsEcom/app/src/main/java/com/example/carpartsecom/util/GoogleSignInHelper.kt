package com.example.carpartsecom.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Helper class for Google Sign-In using Play Services Auth
 */
class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
        const val RC_SIGN_IN = 9001

        // Web Application OAuth Client ID from Google Cloud Console
        const val WEB_CLIENT_ID = "242829987510-pp4mi6fnq5mtos5utdrt0m99dvm1v66r.apps.googleusercontent.com"
    }

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    data class GoogleSignInResult(
        val idToken: String,
        val email: String,
        val displayName: String?,
        val givenName: String?,
        val familyName: String?,
        val profilePictureUrl: String?
    )

    /**
     * Get the sign-in intent to launch
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Handle the result from the sign-in activity
     */
    fun handleSignInResult(data: Intent?): Result<GoogleSignInResult> {
        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null && account.idToken != null) {
                val result = GoogleSignInResult(
                    idToken = account.idToken!!,
                    email = account.email ?: "",
                    displayName = account.displayName,
                    givenName = account.givenName,
                    familyName = account.familyName,
                    profilePictureUrl = account.photoUrl?.toString()
                )
                Log.d(TAG, "Google Sign-In successful for: ${result.email}")
                Result.success(result)
            } else {
                Log.e(TAG, "Google Sign-In failed: No ID token")
                Result.failure(Exception("Failed to get ID token"))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with code: ${e.statusCode}", e)
            val errorMessage = when (e.statusCode) {
                12500 -> "Google Sign-In failed. Please update Google Play Services."
                12501 -> "Sign-in cancelled by user"
                12502 -> "Sign-in currently in progress"
                7 -> "Network error. Please check your connection."
                10 -> "Developer error: Check your OAuth configuration in Google Cloud Console."
                else -> "Google Sign-In failed (code: ${e.statusCode})"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(Exception("Sign-In error: ${e.message}"))
        }
    }

    /**
     * Sign out from Google
     */
    fun signOut() {
        googleSignInClient.signOut()
    }

    /**
     * Revoke access
     */
    fun revokeAccess() {
        googleSignInClient.revokeAccess()
    }

    /**
     * Check if user is already signed in
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}

