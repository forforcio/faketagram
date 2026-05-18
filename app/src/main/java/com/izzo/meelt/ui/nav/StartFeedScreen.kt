package com.izzo.meelt.ui.nav

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izzo.meelt.R
import com.izzo.meelt.data.UsersUiState
import com.izzo.meelt.data.model.User
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import com.izzo.meelt.ui.components.UserImage


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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiary
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
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 66.dp, bottom = 12.dp)
            ) {
                items(users) { user ->
                    SocialPostCard(
                        user,
                        onClick = { onUserPhotoClicked(user) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.feed_near_you),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterStart),
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
            .padding(0.dp)
            .fillMaxWidth()
    ) {
        UserImage(
            user = user,
            contentDescription = stringResource(R.string.content_desc_user_photo),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() },
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(28.dp)) {
            Text(
                text = stringResource(R.string.common_user_name_age, user.username, user.age),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    fontSize = 34.sp
                ),
                color = Color.White,
            )
        }
    }
}
