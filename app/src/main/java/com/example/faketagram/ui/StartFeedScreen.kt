package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.faketagram.R
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.User


@Composable
fun StartFeedScreen(
    uiState: UsersUiState,
    onUserPhotoClicked: (User) -> Unit,
    modifier: Modifier
) {
    val users: List<User> = uiState.getUsersExceptCurrent()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column() {
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
                        contentDescription = stringResource(R.string.content_desc_app_logo),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.feed_near_you),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .width(250.dp)
                .padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(users) { user ->
                SocialPostCard(
                    user,
                    onClick = { onUserPhotoClicked(user) }
                )
            }
        }
    }
}


@Composable
fun SocialPostCard(
    user: User,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Image(
            painter = painterResource(user.resId),
            contentDescription = stringResource(R.string.content_desc_user_photo),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() },
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.common_user_name_age, user.username, user.age),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
