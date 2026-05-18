package com.izzo.meelt.notifications

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChatFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Keep in-app listener as source of truth while app is active to avoid duplicate alerts.
        if (isAppInForeground()) return

        val data = remoteMessage.data

        val senderName = data["senderName"]
            ?: remoteMessage.notification?.title
            ?: return

        val messagePreview = data["messagePreview"]
            ?: data["text"]
            ?: remoteMessage.notification?.body
            ?: return

        val senderUserId = data["senderUserId"]?.toIntOrNull() ?: 0

        ChatNotificationHelper.showIncomingMessage(
            context = applicationContext,
            senderName = senderName,
            senderUserId = senderUserId,
            messagePreview = messagePreview
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenRegistrar.syncRefreshedToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        val runningProcesses = activityManager.runningAppProcesses ?: return false

        return runningProcesses.any { process ->
            process.processName == packageName &&
                process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    private companion object {
        const val TAG = "ChatFcmService"
    }
}


