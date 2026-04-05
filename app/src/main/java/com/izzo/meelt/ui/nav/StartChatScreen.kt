package com.izzo.meelt.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.izzo.meelt.R
import com.izzo.meelt.data.UsersUiState
import com.izzo.meelt.data.model.Chat
import com.izzo.meelt.data.model.User
import com.izzo.meelt.ui.components.UserImage

@Composable
fun StartChatScreen(
    uiState: UsersUiState,
    onUserClick: (User) -> Unit,
    onBlockUser: (User) -> Unit,
    modifier: Modifier
) {
    val chats: List<Chat> = remember(uiState) {
        uiState.getChatsOrdered()
    }
    val currentUser = uiState.getAuthenticatedUser()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(
                            MaterialTheme.colorScheme.primary
                        )
                        .padding(top = 25.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.meelt_logo_small),
                        contentDescription = stringResource(R.string.content_desc_app_logo),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter),
                    )
                }
                Box (
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row {
                        if (currentUser != null) {
                            UserImage(
                                user = currentUser,
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
                        } else {
                            Image(
                                painter = painterResource(R.drawable.default_user),
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
                        }
                        Text(
                            text = uiState.getAuthenticatedUser()?.username
                                ?: stringResource(R.string.chat_user_not_found),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                }
                Text(
                    text = stringResource(R.string.chat_title_messages),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
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
                    onBlockClick = {
                        onBlockUser(chat.interlocutor)
                    },
                    lastMessage = lastMessageText(chat),
                    messagesUnread = chat.getPendingMessagesCount()
                )
            }
        }
    }
}

@Composable
fun lastMessageText(chat: Chat): String {
    val lastMessage = chat.getLastMessage()
    return when {
        lastMessage == null -> stringResource(R.string.chat_no_messages_yet)
        !lastMessage.imageUrl.isNullOrBlank() -> stringResource(R.string.chat_message_image)
        else -> lastMessage.text ?: ""
    }
}

@Composable
fun ChatView(
    user: User,
    onClick: () -> Unit,
    onBlockClick: () -> Unit,
    lastMessage: String,
    messagesUnread: Int
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 0.dp,
                horizontal = 20.dp
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                UserImage(
                    user = user,
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
                            color = Color.White
                        ),
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
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

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.profile_action_block)) },
                onClick = {
                    menuExpanded = false
                    onBlockClick()
                }
            )
        }
    }
}
