package com.example.faketagram.data

import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User

data class UsersUiState(
    val users: List<User>,
    val messages: List<Message> = emptyList(),
    val currentUserUid: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {

    fun getUserById(userId: Int): User {
        val user = users.find { it.userId == userId }
        if (user == null) {
            return User(
                userId = 0,
                username = "Unexistent user",
                age = 0,
                bio = "this user does not exist",
                resName = "wenaso_1",
            )
        }
        return user
    }

    fun getUserByFirebaseUid(firebaseUid: String): User {
        val user = users.find { it.firebaseUid == firebaseUid }
        if (user == null) {
            return User(
                userId = 0,
                username = "Unexistent user",
                age = 0,
                bio = "this user does not exist",
                resName = "wenaso_1",
            )
        }
        return user
    }

    fun getIncomingMessagesForCurrentUser(senderId: String): List<Message> {
        val otherUid = users.find { it.firebaseUid == senderId }?.firebaseUid ?: return emptyList()
        if (currentUserUid.isBlank()) return emptyList()

        return messages
            .filter { m ->
                (m.senderUid == currentUserUid && m.receiverUid == otherUid) ||
                        (m.senderUid == otherUid && m.receiverUid == currentUserUid)
            }
            .sortedByDescending { it.timestamp }
    }
}