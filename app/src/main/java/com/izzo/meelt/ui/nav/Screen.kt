package com.izzo.meelt.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

enum class Screen(
    val icon: ImageVector,
) {
    HOME(Icons.Default.Home),
    CHAT(Icons.Default.ChatBubbleOutline),
    PROFILE(Icons.Default.AccountCircle)
}

@Serializable
data class UserProfileRoute(
    val userId: Int,
)

@Serializable
data class UserChatRoute(
    val userId: Int,
)
