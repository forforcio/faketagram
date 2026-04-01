package com.izzo.meelt.ui.nav

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izzo.meelt.R
import com.izzo.meelt.data.model.User
import java.util.Locale

@Composable
fun StartUserProfileScreen(
    user: User,
    modifier: Modifier = Modifier,
    onChatClick: (User) -> Unit,
    onBlockClick: (User) -> Unit,
    showBottomActions: Boolean = true,
) {
    val maxPhotoHeight = LocalConfiguration.current.screenHeightDp.dp * 0.72f

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
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(top = 25.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.meelt_logo),
                    contentDescription = stringResource(R.string.content_desc_app_logo),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = if (showBottomActions) 130.dp else 24.dp
                )
            ) {
                itemsIndexed(photos) { index, photoResId ->
                    ProfilePhotoCard(
                        photoResId = photoResId,
                        maxPhotoHeight = maxPhotoHeight,
                        overlay = {
                            when {
                                index == 0 -> MainProfileOverlay(user = user)
                                index == 1 -> AboutMeOverlay(bio = user.bio)
                            }
                        }
                    )
                }
            }

            if (showBottomActions) {
                BottomActionsOverlay(
                    onChatClick = { onChatClick(user) },
                    onBlockClick = { onBlockClick(user) }
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoCard(
    photoResId: Int,
    maxPhotoHeight: androidx.compose.ui.unit.Dp,
    overlay: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxPhotoHeight)
    ) {
        Image(
            painter = painterResource(photoResId),
            contentDescription = stringResource(R.string.content_desc_profile_photo),
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
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
            text = stringResource(R.string.common_user_name_age, user.username, user.age),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.profile_distance, formatDistanceKm(user.distance)),
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
            text = stringResource(R.string.profile_about_me),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 15.sp
            ),
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
            .padding(start = 20.dp, end = 20.dp, bottom = 52.dp)
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
            contentDescription = stringResource(R.string.profile_action_open_chat),
            onClick = onChatClick
        )
        ProfileActionIcon(
            imageVector = Icons.Rounded.Stars,
            contentDescription = stringResource(R.string.profile_action_favorite),
            onClick = {}
        )
        ProfileActionIcon(
            imageVector = Icons.Rounded.Block,
            contentDescription = stringResource(R.string.profile_action_block),
            onClick = onBlockClick
        )
    }
}

@Composable
private fun ProfileActionIcon(
    imageVector: ImageVector,
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

