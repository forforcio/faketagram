package com.example.faketagram.data

import android.content.Context
import com.example.faketagram.R
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

    fun getUserById(userId: Int, context: Context? = null): User {
        val user = users.find { it.userId == userId }
        if (user == null) {
            return User(
                userId = 0,
                username = context?.getString(R.string.fallback_user_name) ?: "Usuario no encontrado",
                age = 0,
                distance = 0.0,
                bio = context?.getString(R.string.fallback_user_bio) ?: "Este usuario no existe",
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

    fun getIncomingMessagesForCurrentUser(senderId: String, context: Context? = null): List<Message> {
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
                    text = context?.getString(R.string.fallback_message_demo_1)
                        ?: "Que casualidad, yo tambien soy un mensaje de prueba!",
                    senderUid = authenticatedUserUid,
                    receiverUid = otherUid,
                    timestamp = System.currentTimeMillis(),
                ),
                Message(
                    text = context?.getString(R.string.fallback_message_demo_2)
                        ?: "Hola soy un mensaje de prueba",
                    senderUid = otherUid,
                    receiverUid = authenticatedUserUid,
                    timestamp = System.currentTimeMillis(),
                )
            )
            return defaultList
        } else {
            return result
        }
    }
}