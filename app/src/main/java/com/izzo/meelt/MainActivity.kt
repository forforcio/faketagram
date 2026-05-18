package com.izzo.meelt

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.izzo.meelt.notifications.ChatNotificationHelper
import com.izzo.meelt.notifications.FcmTokenRegistrar
import com.izzo.meelt.ui.theme.MeeltTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage

// 10.0.2.2 for local, 127.0.0.1 for real device
private const val hostAddress = "127.0.0.1"

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var pendingChatUserId by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

//        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
//            Firebase.database.useEmulator(hostAddress, 9000)
//            Firebase.auth.useEmulator(hostAddress, 9099)
//            Firebase.storage.useEmulator(hostAddress, 9199)
//        }

        ChatNotificationHelper.createChannel(this)
        requestNotificationPermissionIfNeeded()

        // Read userId from notification tap (app cold start)
        pendingChatUserId = intent
            .takeIf { it.hasExtra(ChatNotificationHelper.EXTRA_CHAT_USER_ID) }
            ?.getIntExtra(ChatNotificationHelper.EXTRA_CHAT_USER_ID, 0)
            ?.takeIf { it != 0 }

        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        FcmTokenRegistrar.syncCurrentTokenForSignedInUser()

        enableEdgeToEdge()
        setContent {
            MeeltTheme {
                MeeltApp(
                    initialChatUserId = pendingChatUserId,
                    onInitialChatConsumed = { pendingChatUserId = null },
                    onLogoutRequested = { navigateToSignInAndFinish() }
                )
            }
        }
    }

    // Called when app is already running and notification is tapped (singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingChatUserId = intent
            .takeIf { it.hasExtra(ChatNotificationHelper.EXTRA_CHAT_USER_ID) }
            ?.getIntExtra(ChatNotificationHelper.EXTRA_CHAT_USER_ID, 0)
            ?.takeIf { it != 0 }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
    }

    private fun navigateToSignInAndFinish() {
        val intent = Intent(this, SignInActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MeeltTheme {}
}