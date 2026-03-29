package com.example.faketagram.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.faketagram.R
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.Chat
import com.example.faketagram.data.model.User

@Composable
fun StartChatScreen(
    uiState: UsersUiState,
    onUserClick: (User) -> Unit,
    modifier: Modifier
) {
    val chats: List<Chat> = remember(uiState) {
        uiState.getChatsOrdered()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                Box {
                    Row {
                        Image(
                            painter = painterResource(
                                uiState.getCurrentUserProfilePicture()
                                    ?: R.drawable.default_user
                            ),
                            contentDescription = stringResource(R.string.content_desc_user_photo),
                            modifier = Modifier
                                .clip(CircleShape)
                                .requiredSize(28.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = uiState.getAuthenticatedUser()?.username
                                ?: stringResource(R.string.chat_user_not_found),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            ),
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_search),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                    )
                }
                Text(
                    text = stringResource(R.string.chat_title_messages),
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
            items(chats) { chat ->
                ChatView(
                    chat.interlocutor,
                    onClick = {
                        onUserClick(chat.interlocutor)
                    },
                    lastMessage = chat.getLastMessage()?.text?: stringResource(R.string.chat_no_messages_yet),
                    messagesUnread = chat.getPendingMessagesCount()
                )
            }
        }
    }
}

@Composable
fun ChatView(
    user: User,
    onClick: () -> Unit,
    lastMessage: String,
    messagesUnread: Int
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Image(
                    painter = painterResource(user.resId),
                    contentDescription = stringResource(R.string.content_desc_user_photo),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 8.dp)
                        .fillMaxSize(0.8f)
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        ),
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black,
                            fontWeight = if (messagesUnread > 0) {
                                FontWeight.ExtraBold
                            } else FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            Box {
                if (messagesUnread > 0) {
                    Text(
                        text = messagesUnread.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
