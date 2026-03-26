package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faketagram.R
import com.example.faketagram.data.model.User
import java.util.Locale

@Composable
fun StartUserProfileScreen(
    user: User,
    modifier: Modifier = Modifier,
    onChatClick: (User) -> Unit,
    onBlockClick: (User) -> Unit,
) {
    val photos = buildList {
        add(user.resId)
        addAll(user.galleryResIds)
    }.distinct().filter { it != 0 }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Color.Transparent
                    )
                    .padding(top = 25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.meelt_logo),
                    contentDescription = "User photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 130.dp
            )
        ) {
            itemsIndexed(photos) { index, photoResId ->
                ProfilePhotoCard(
                    photoResId = photoResId,
                    overlay = {
                        when {
                            index == 0 -> MainProfileOverlay(user = user)
                            index == 1 -> AboutMeOverlay(bio = user.bio)
                        }

                        if (index == photos.lastIndex) {
                            BottomActionsOverlay(
                                onChatClick = { onChatClick(user) },
                                onBlockClick = { onBlockClick(user) }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoCard(
    photoResId: Int,
    overlay: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(photoResId),
            contentDescription = "Profile photo",
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.58f)
                        )
                    )
                )
        )

        overlay()
    }
}

@Composable
private fun BoxScope.MainProfileOverlay(user: User) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${user.username}, ${user.age}",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "a ${formatDistanceKm(user.distance)} km de distancia",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
private fun BoxScope.AboutMeOverlay(bio: String) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sobre mí",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
private fun BoxScope.BottomActionsOverlay(
    onChatClick: () -> Unit,
    onBlockClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(20.dp)
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(29.dp)
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileActionIcon(
            imageVector = Icons.AutoMirrored.Rounded.Chat,
            contentDescription = "Open chat",
            onClick = onChatClick
        )
        ProfileActionIcon(
            imageVector = Icons.Rounded.Stars,
            contentDescription = "Favorite",
            onClick = {}
        )
        ProfileActionIcon(
            imageVector = Icons.Rounded.Block,
            contentDescription = "Block",
            onClick = onBlockClick
        )
    }
}

@Composable
private fun ProfileActionIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatDistanceKm(distance: Double): String {
    return String.format(Locale.US, "%.1f", distance)
}
