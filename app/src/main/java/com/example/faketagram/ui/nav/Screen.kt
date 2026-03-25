package com.example.faketagram.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    CHAT("Chat", Icons.Default.ChatBubbleOutline),
    PROFILE("Profile", Icons.Default.AccountCircle)
}