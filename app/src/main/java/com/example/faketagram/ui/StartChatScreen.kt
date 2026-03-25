package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.faketagram.R
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.User

@Composable
fun StartChatScreen(
    uiState: UsersUiState,
    onUserClick: (User) -> Unit,
    modifier: Modifier
) {
    val users: List<User> = uiState.getUsersExceptCurrent()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
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
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Color.LightGray
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "\uD83D\uDD0D\uFE0E Buscar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                    )
                }
                Text (
                    text = "Mensajes",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .padding(vertical = 15.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            items(users) { user ->
                ChatView(
                    user,
                    onClick = {
                        onUserClick(user)
                    }
                )
            }
        }
    }
}

@Composable
fun ChatView(
    user: User,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 0.dp,
                horizontal = 20.dp
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
    ) {
        Row(
            modifier = Modifier
        ) {
            Image(
                painter = painterResource(user.resId),
                contentDescription = "User photo",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(50.dp)
                    .clip(CircleShape)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box {
                Column(modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 8.dp)
                    .fillMaxSize()
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Ultimo mensaje leído (no implementado)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black
                        ),
                        overflow= TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
