package com.example.faketagram.data

import com.example.faketagram.data.model.User

data class UsersUiState(
    val users: List<User>, var currentChatUserId: Int = 0
) {
    fun getUserById(userId: Int): User {
        val user = users.find { it.userId == userId }
        if (user == null) { // TODO: restructure default user
            return User(
                0,
                "Unexistent user",
                0,
                "this user does not exist",
                "wenaso_1")
        }
        return user
    }
}