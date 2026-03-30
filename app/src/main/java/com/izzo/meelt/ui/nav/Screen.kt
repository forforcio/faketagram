package com.izzo.meelt.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.izzo.meelt.R
import kotlinx.serialization.Serializable

enum class Screen(
    val icon: ImageVector? = null,
    @param:DrawableRes val iconRes: Int? = null,
) {
    HOME(icon = Icons.Default.Home),
    CHAT(iconRes = R.drawable.chats_icon),
    PROFILE(icon = Icons.Default.AccountCircle)
}

@Serializable
data class UserProfileRoute(
    val userId: Int,
)

@Serializable
data class UserChatRoute(
    val userId: Int,
)
