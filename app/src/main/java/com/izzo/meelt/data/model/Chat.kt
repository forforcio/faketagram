package com.izzo.meelt.data.model

import androidx.compose.ui.res.stringResource
import com.izzo.meelt.R

class Chat(
    val interlocutor: User,
    val messages: List<Message> = emptyList()
) {

    fun getMessagesReverseChronological(): List<Message> {
        return messages.sortedByDescending { it.timestamp }
    }

    fun getLastMessage(): Message? {
        return messages.lastOrNull()
    }

    fun getPendingMessagesCount(): Int {
        return messages.count { it.senderUid == interlocutor.firebaseUid && !it.read }
    }
}