package com.example.faketagram.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User
import com.example.faketagram.ui.model.UserChat

@Composable
fun StartUserChatScreen(
    uiState: UsersUiState, userId: Int, modifier: Modifier
) {
    val user: User = uiState.getUserById(userId)

    Scaffold(
        modifier = modifier.fillMaxSize(), topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = user.username,
                    modifier = Modifier.align(Alignment.BottomStart),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            val messages = listOf(Message("hola goapa"), Message("me xupas la pixa? xdd"))
            items(messages) { message ->
                MessageDisplay(
                    user = user,
                    message.content
                )
            }
        }
    }
}


@Composable
fun MessageDisplay(
    user: User,
    message: String
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = message, style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}
