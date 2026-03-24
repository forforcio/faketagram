package com.example.faketagram.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User
import java.io.File

@Composable
fun StartUserChatScreen(
    uiState: UsersUiState,
    userId: Int,
    modifier: Modifier,
    onSendMessage: (String) -> Unit,
    onSendPhoto: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val user: User = uiState.getUserById(userId)
    val messages = uiState.getIncomingMessagesForCurrentUser(
        senderId = user.firebaseUid
    )
    var textToSend by rememberSaveable { mutableStateOf("") }
    var photoMenuExpanded by remember { mutableStateOf(false) }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onSendPhoto)
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraTempUri?.let(onSendPhoto)
        }
    }

    val sendCurrentMessage = {
        val cleanedText = textToSend.trim()
        if (cleanedText.isNotEmpty()) {
            onSendMessage(cleanedText)
            textToSend = ""
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(), topBar = {
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .imePadding()
            ) {
                val isCompactComposer = maxWidth < 360.dp
                val composerSpacing = if (isCompactComposer) 4.dp else 8.dp
                val actionButtonPadding = if (isCompactComposer) {
                    ButtonDefaults.ButtonWithIconContentPadding
                } else {
                    ButtonDefaults.ContentPadding
                }
                val sendButtonLabel = if (isCompactComposer) "Env." else "Enviar"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(composerSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = textToSend,
                        onValueChange = { textToSend = it },
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 0.dp),
                        placeholder = { Text("Escribe un mensaje...") },
                        singleLine = true,
                    )
                    Button(
                        onClick = sendCurrentMessage,
                        contentPadding = actionButtonPadding,
                    ) {
                        Text(sendButtonLabel)
                    }
                    Box {
                        Button(
                            onClick = { photoMenuExpanded = true },
                            contentPadding = actionButtonPadding,
                        ) {
                            Text("Foto")
                        }
                        DropdownMenu(
                            expanded = photoMenuExpanded,
                            onDismissRequest = { photoMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Galeria") },
                                onClick = {
                                    photoMenuExpanded = false
                                    pickPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Camara") },
                                onClick = {
                                    photoMenuExpanded = false
                                    val outputUri = createTempImageUri(context)
                                    cameraTempUri = outputUri
                                    takePhotoLauncher.launch(outputUri)
                                }
                            )
                        }
                    }
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
                val sender: User = uiState.getUserByFirebaseUid(message.senderUid ?: "")
                MessageDisplay(
                    user = sender,
                    message = message,
                    isReceived = sender == user
                )
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "chat_images").apply { mkdirs() }
    val imageFile = File.createTempFile("chat_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
fun MessageDisplay(
    user: User,
    message: Message,
    isReceived: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (isReceived) {
            Image(
                painter = painterResource(user.resId),
                contentDescription = "User photo",
                modifier = Modifier
                    .clip(CircleShape)
                    .requiredSize(50.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                MessageBubble(
                    message = message,
                    modifier = Modifier
                )
            }
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                MessageBubble(
                    message = message,
                    modifier = Modifier
                )
            }
            Image(
                painter = painterResource(user.resId),
                contentDescription = "User photo",
                modifier = Modifier
                    .clip(CircleShape)
                    .requiredSize(50.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        if (!message.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.imageUrl,
                contentDescription = "Photo message",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Card(
                modifier = Modifier.widthIn(max = maxWidth)
            ) {
                Text(
                    text = message.text.orEmpty(),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
