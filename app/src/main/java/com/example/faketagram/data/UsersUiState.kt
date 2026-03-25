package com.example.faketagram.data

import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User

data class UsersUiState(
    private val users: List<User>,
    private val messages: List<Message> = emptyList(),
    private val authenticatedUserUid: String = "",
    private val isLoading: Boolean = false,
    private val error: String? = null,
) {

    fun getAuthenticatedUserUid(): String {
        return authenticatedUserUid
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
        if (authenticatedUserUid.isBlank()) return emptyList()

        return messages
            .filter { m ->
                (m.senderUid == authenticatedUserUid && m.receiverUid == otherUid) ||
                        (m.senderUid == otherUid && m.receiverUid == authenticatedUserUid)
            }
            .sortedByDescending { it.timestamp }
    }
}