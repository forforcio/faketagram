package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.User

@Composable
fun StartUserChatScreen(
    uiState: UsersUiState,
    userId: Int,
    modifier: Modifier,
    onSendMessage: (String) -> Unit,
) {
    val user: User = uiState.getUserById(userId)
    val messages = uiState.getIncomingMessagesForCurrentUser(
        senderId = user.firebaseUid
    )
    var textToSend by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(), topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(20.dp)
                    .imePadding()
            ) {
                Text(
                    text = user.username,
                    modifier = Modifier.align(Alignment.BottomStart),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = textToSend,
                    onValueChange = { textToSend = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        val cleanedText = textToSend.trim()
                        if (cleanedText.isNotEmpty()) {
                            onSendMessage(cleanedText)
                            textToSend = ""
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text("Enviar")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                var sender: User = uiState.getUserByFirebaseUid(message.senderUid ?: "")
                MessageDisplay(
                    user = sender,
                    message = message.text ?: "",
                    isReceived = sender == user
                )
            }
        }
    }
}

@Composable
fun MessageDisplay(
    user: User,
    message: String,
    isReceived: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().imePadding(),
        horizontalArrangement = if (isReceived) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isReceived) {
            Image(
                painter = painterResource(user.resId),
                contentDescription = "User photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            MessageBubble(message)
        } else {
            MessageBubble(message)
            Image(
                painter = painterResource(user.resId),
                contentDescription = "User photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MessageBubble(message: String) {
    Card {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp)
        )
    }
}
