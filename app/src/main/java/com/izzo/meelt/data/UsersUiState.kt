package com.izzo.meelt.data

import android.content.Context
import com.izzo.meelt.R
import com.izzo.meelt.data.model.Chat
import com.izzo.meelt.data.model.Message
import com.izzo.meelt.data.model.User

data class UsersUiState(
    val users: List<User>,
    val messages: List<Message> = emptyList(),
    val pendingImagePreviews: Map<String, String> = emptyMap(),
    val authenticatedUserUid: String = "",
    val availableUsersJsonNames: List<String> = emptyList(),
    val selectedUsersJsonName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {

    fun getAuthenticatedUser(): User? {
        return users.find { it.firebaseUid == authenticatedUserUid }
    }

    fun getUsersExceptCurrent(): List<User> {
        return users.filter { it.firebaseUid != authenticatedUserUid }
    }

    fun getCurrentUserProfilePicture(): Int? {
        val currentUser = users.find { it.firebaseUid == authenticatedUserUid }
        return currentUser?.resId
    }

    fun getUserById(userId: Int): User {
        val user = users.find { it.userId == userId }
        if (user == null) {
            return User(
                userId = 0,
                username = "Usuario no encontrado",
                age = 0,
                distance = 0.0,
                bio = "Este usuario no existe",
                resName = "wenaso_1",
            )
        }
        return user
    }

    fun isUserBlocked(userId: Int): Boolean {
        return users.find { it.userId == userId }?.isBlocked == true
    }

    fun getUserByFirebaseUid(firebaseUid: String, context: Context? = null): User {
        val user = users.find { it.firebaseUid == firebaseUid }
        if (user == null) {
            return User(
                userId = 0,
                username = context?.getString(R.string.fallback_user_name) ?: "Usuario no encontrado",
                age = 0,
                distance = 0.0,
                bio = context?.getString(R.string.fallback_user_bio) ?: "Este usuario no existe",
                resName = "default_user",
            )
        }
        return user
    }

    fun getIncomingMessagesForCurrentUser(senderId: String): List<Message> {
        val otherUid = users.find { it.firebaseUid == senderId }?.firebaseUid ?: return emptyList()
        if (authenticatedUserUid.isBlank()) return emptyList()

        return messages
            .filter { m ->
                (m.senderUid == authenticatedUserUid && m.receiverUid == otherUid) ||
                        (m.senderUid == otherUid && m.receiverUid == authenticatedUserUid)
            }
            .sortedBy { it.timestamp }
    }

    fun getPendingImagePreview(messageId: String?): String? {
        if (messageId.isNullOrBlank()) return null
        return pendingImagePreviews[messageId]
    }

    fun getChatsOrdered(): List<Chat> {
        val interlocutors = getUsersExceptCurrent()
        val chats: MutableList<Chat> = mutableListOf()
        for (interlocutor in interlocutors) {
            val chat = getChatByUserId(interlocutor.userId)
            if (chat.messages.isNotEmpty()) {
                chats.add(chat)
            }
        }
        return chats.sortedByDescending { chat ->
            chat.getLastMessage()?.timestamp ?: 0L
        }
    }

    fun getChatByUserId(userId: Int): Chat {
        val user = getUserById(userId)
        val chatMessages = getIncomingMessagesForCurrentUser(user.firebaseUid)
        return Chat(
            interlocutor = user,
            messages = chatMessages
        )
    }
}