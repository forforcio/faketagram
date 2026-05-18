package com.izzo.meelt.notifications

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.messaging

object FcmTokenRegistrar {
    private const val TAG = "FcmTokenRegistrar"
    private const val TOKENS_CHILD = "fcmTokens"

    fun syncCurrentTokenForSignedInUser() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        Firebase.messaging.token
            .addOnSuccessListener { token ->
                persistToken(uid = uid, token = token)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Unable to fetch current FCM token", exception)
            }
    }

    fun syncRefreshedToken(token: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        persistToken(uid = uid, token = token)
    }

    private fun persistToken(uid: String, token: String) {
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener { installationId ->
                val payload = mapOf(
                    "token" to token,
                    "platform" to "android",
                    "updatedAt" to ServerValue.TIMESTAMP
                )

                Firebase.database.reference
                    .child(TOKENS_CHILD)
                    .child(uid)
                    .child(installationId)
                    .setValue(payload)
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Unable to persist FCM token", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Unable to resolve Firebase installation id", exception)
            }
    }
}

