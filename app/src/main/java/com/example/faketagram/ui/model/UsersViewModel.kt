package com.example.faketagram.ui.model

import androidx.lifecycle.ViewModel
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.User
import com.example.faketagram.data.service.DataManagementService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsersViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState(emptyList()))
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    context(dataService: DataManagementService)
    fun init() {
        _uiState.update { currentState ->
            currentState.copy(
                users = dataService.getAllUsers()
            )
        }
    }

    fun setCurrentChatUser(userId: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                currentChatUserId = userId
            )
        }
    }

    fun getCurrentChatUser(): User? {
        return getUserById(_uiState.value.currentChatUserId)
    }

    fun getUserById(userId: Int): User? {
        return _uiState.value.users.find { it.userId == userId }
    }

    fun getAllUsers(): List<User> {
        return _uiState.value.users
    }

}