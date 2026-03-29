package com.izzo.meelt.ui.nav

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.izzo.meelt.R
import com.izzo.meelt.data.UsersUiState
import com.izzo.meelt.data.model.Chat
import com.izzo.meelt.data.model.Message
import com.izzo.meelt.data.model.User
import java.io.File

@Composable
fun StartUserChatScreen(
    uiState: UsersUiState,
    chat: Chat,
    modifier: Modifier,
    onSendMessage: (String) -> Unit,
    onSendPhoto: (Uri) -> Unit,
    onChatOpened: (Int) -> Unit = {},
    onChatClosed: () -> Unit = {},
    deleteMessage: (Message) -> Unit = {}
) {
    val context = LocalContext.current
    var textToSend by rememberSaveable { mutableStateOf("") }
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(chat.interlocutor.userId) {
        onChatOpened(chat.interlocutor.userId)
        onDispose { onChatClosed() }
    }

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

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary
                        )
                        .padding(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(top = 20.dp).fillMaxSize().padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.chat_back_icon),
                            tint = Color.White
                        )
                        Image(
                            painter = painterResource(chat.interlocutor.resId),
                            contentDescription = stringResource(R.string.content_desc_user_photo),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clip(CircleShape)
                                .requiredSize(50.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column() {
                            Text(
                                text = chat.interlocutor.username,
                                modifier = Modifier,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = stringResource(R.string.chat_distance_from_you, chat.interlocutor.distance),
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(15.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_search),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                    )
                }
            }
        },
        bottomBar = {
            Box (
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = textToSend,
                        onValueChange = { textToSend = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            errorContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowCircleRight,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = stringResource(R.string.chat_send_icon),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(
                                onClick = {
                                    val cleanedText = textToSend.trim()
                                    if (cleanedText.isNotEmpty()) {
                                        onSendMessage(cleanedText)
                                        textToSend = ""
                                    }
                                }
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = stringResource(R.string.chat_take_photo_icon),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(
                                onClick = {
                                    val outputUri = createTempImageUri(context)
                                    cameraTempUri = outputUri
                                    takePhotoLauncher.launch(outputUri)
                                }
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.Image,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = stringResource(R.string.chat_gallery_search_icon),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(
                                onClick = {
                                    pickPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            reverseLayout = true
        ) {
            items(chat.getMessagesReverseChronological()) { message ->
                val sender: User = uiState.getUserByFirebaseUid(message.senderUid ?: "", context)
                MessageDisplay(
                    user = sender,
                    message = message,
                    isReceived = sender == chat.interlocutor,
                    deleteMessage = { deleteMessage(message) }
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
    isReceived: Boolean,
    deleteMessage: (Message) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isReceived) {
            Image(
                painter = painterResource(user.resId),
                contentDescription = stringResource(R.string.content_desc_user_photo),
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
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontColor = Color.White,
                    deleteMessage = deleteMessage
                )
            }
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                MessageBubble(
                    message = message,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.surface,
                    fontColor = Color.Black,
                    deleteMessage = deleteMessage
                )
            }
            Image(
                painter = painterResource(user.resId),
                contentDescription = stringResource(R.string.content_desc_user_photo),
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
    modifier: Modifier = Modifier,
    color: Color,
    fontColor: Color,
    deleteMessage: (Message) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.padding(vertical = 4.dp).combinedClickable(
            onClick = {},
            onLongClick = { deleteMessage(message) },
        )
    ) {
        if (!message.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.imageUrl,
                contentDescription = stringResource(R.string.chat_photo_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Card(
                modifier = Modifier.widthIn(max = maxWidth),
                colors = CardDefaults.cardColors(color)
            ) {
                Text(
                    text = message.text.orEmpty(),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = fontColor
                    )
                )
            }
        }
    }
}
