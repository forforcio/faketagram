package com.example.faketagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.content.ContextCompat
import com.example.faketagram.notifications.NotificationHelper
import com.example.faketagram.ui.chat.ChatScreen
import com.example.faketagram.ui.messages.MessagesScreen
import com.example.faketagram.ui.theme.FaketagramTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        val initialSenderId = intent.getStringExtra(NotificationHelper.EXTRA_SENDER_ID)
        setContent {
            FaketagramTheme {
                FaketagramApp(initialChatSenderId = initialSenderId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val senderId = intent.getStringExtra(NotificationHelper.EXTRA_SENDER_ID)
        if (senderId != null) {
            setContent {
                FaketagramTheme {
                    FaketagramApp(initialChatSenderId = senderId)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun FaketagramApp(initialChatSenderId: String? = null) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var currentChatSenderId by rememberSaveable { mutableStateOf(initialChatSenderId) }

    val senderId = currentChatSenderId
    if (senderId != null) {
        ChatScreen(
            senderId = senderId,
            onBack = {
                currentChatSenderId = null
                currentDestination = AppDestinations.MESSAGES
            }
        )
        return
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> Text(
                    text = "Home Feed",
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.MESSAGES -> MessagesScreen(
                    onChatSelected = { senderId -> currentChatSenderId = senderId }
                )
                AppDestinations.FAVORITES -> Text(
                    text = "Favorites",
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.PROFILE -> Text(
                    text = "Profile",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    MESSAGES("Messages", Icons.Default.Email),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}