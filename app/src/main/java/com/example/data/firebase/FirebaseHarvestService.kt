package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.FarmProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseHarvestService {
    private const val TAG = "FirebaseHarvest"

    fun getFirestore(context: Context): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        val app = FirebaseApp.getInstance()
        return FirebaseFirestore.getInstance(app, databaseId)
    }

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun attemptAutoSignIn(
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        val user = currentUser
        if (user != null) {
            onSuccess(user)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id not yet generated: ${e.message}")
            onUnauthenticated()
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { onSuccess(it) } ?: onUnauthenticated()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit,
        scope: CoroutineScope
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { user ->
                        // Record user profile document
                        syncUserProfile(activity, user)
                        onSuccess(user)
                    } ?: onError("Sign-in succeeded but user was null")
                } else {
                    onError("Unexpected credential format received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In dismissed: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit, scope: CoroutineScope) {
        auth.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed clearing credential state: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    private suspend fun syncUserProfile(context: Context, user: FirebaseUser) = withContext(Dispatchers.IO) {
        try {
            val db = getFirestore(context)
            val userRef = db.collection("users").document(user.uid)
            val data = mapOf(
                "userId" to user.uid,
                "email" to (user.email ?: "unknown@theharvest.ai"),
                "displayName" to (user.displayName ?: "Farmer"),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Could not sync user profile: ${e.message}")
        }
    }

    suspend fun syncFarmToFirestore(context: Context, farm: FarmProfile): Boolean = withContext(Dispatchers.IO) {
        val user = currentUser ?: return@withContext false
        try {
            val db = getFirestore(context)
            val farmRef = db.collection("users")
                .document(user.uid)
                .collection("farms")
                .document(farm.id)

            val farmData = mapOf(
                "id" to farm.id,
                "userId" to user.uid,
                "name" to farm.name,
                "scale" to farm.scale.name.lowercase(),
                "cropName" to farm.cropName,
                "cropScientificName" to farm.cropScientificName,
                "lifecycle" to farm.lifecycle.name.lowercase(),
                "isOrganic" to farm.isOrganic,
                "areaAcres" to farm.areaAcres,
                "latitude" to farm.location.latitude,
                "longitude" to farm.location.longitude,
                "locationName" to farm.locationName,
                "soilType" to farm.soilType,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            farmRef.set(farmData, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing farm to Firestore: ${e.message}", e)
            false
        }
    }
}
