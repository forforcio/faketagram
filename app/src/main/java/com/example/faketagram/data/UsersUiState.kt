package com.example.faketagram.data

import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User

data class UsersUiState(
    val users: List<User>,
    val messages: List<Message> = emptyList(),
    val authenticatedUserUid: String = "",
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
                username = "Unexistent user",
                age = 0,
                distance = 0.0,
                bio = "this user does not exist",
                resName = "wenaso_1",
            )
        }
        return user
    }

    fun isUserBlocked(userId: Int): Boolean {
        return users.find { it.userId == userId }?.isBlocked == true
    }

    fun getUserByFirebaseUid(firebaseUid: String): User {
        val user = users.find { it.firebaseUid == firebaseUid }
        if (user == null) {
            return User(
                userId = 0,
                username = "Unexistent user",
                age = 0,
                distance = 0.0,
                bio = "this user does not exist",
                resName = "default_user",
            )
        }
        return user
    }

    fun getIncomingMessagesForCurrentUser(senderId: String): List<Message> {
        val otherUid = users.find { it.firebaseUid == senderId }?.firebaseUid ?: return emptyList()
        if (authenticatedUserUid.isBlank()) return emptyList()

        val result = messages
            .filter { m ->
                (m.senderUid == authenticatedUserUid && m.receiverUid == otherUid) ||
                        (m.senderUid == otherUid && m.receiverUid == authenticatedUserUid)
            }
            .sortedByDescending { it.timestamp }

        if (result.isEmpty()) {
            val defaultList: List<Message> = listOf(
                Message(
                    text = "Que casualidad, yo también soy un mensaje de prueba!",
                    senderUid = authenticatedUserUid,
                    receiverUid = otherUid,
                    timestamp = System.currentTimeMillis()
                ),
                Message(
                    text = "Hola soy un mensaje de prueba",
                    senderUid = otherUid,
                    receiverUid = authenticatedUserUid,
                    timestamp = System.currentTimeMillis()
                )
            )
            return defaultList
        } else {
            return result
        }
    }
}