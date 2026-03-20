package com.example.faketagram

import android.R.id.message
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.faketagram.ui.model.UsersViewModel
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.faketagram.data.model.Chat
import com.example.faketagram.data.model.Message
import com.example.faketagram.data.model.User
import com.example.faketagram.data.service.DataManagementService
import com.example.faketagram.data.service.ResourcesService
import com.example.faketagram.ui.StartChatScreen
import com.example.faketagram.ui.StartFeedScreen
import com.example.faketagram.ui.StartUserChatScreen
import com.example.faketagram.ui.model.UserChat
import com.example.faketagram.ui.nav.Screen

@PreviewScreenSizes
@Composable
fun FaketagramApp(
    viewModel: UsersViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    var currentDestination by rememberSaveable { mutableStateOf(Screen.HOME) }

    val dataService = DataManagementService()
    val resourcesService = ResourcesService(LocalContext.current)
    context(resourcesService) {
        dataService.getUsersFromJson(R.raw.users)
    }
    context(dataService) {
        viewModel.init()
    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Screen.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                        navController.navigate(it.name)
                    }
                )
            }
        }
    ) {
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = Screen.HOME.name,
            modifier = Modifier
        ) {
            composable(route = Screen.HOME.name) {
                StartFeedScreen(
                    uiState = uiState,
                    onUserPhotoClicked = {
                        navController.navigate(
                            UserChat(
                                userId = it
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            composable(route = Screen.CHAT.name) {
                StartChatScreen(
                    uiState = uiState,
                    onUserClick = {
                        navController.navigate(
                            UserChat(
                                userId = it
                            )
                        )
                    },
                    modifier = Modifier
                )
            }
            composable<UserChat> { backStackEntry ->
                val userChat: UserChat = backStackEntry.toRoute()
                StartUserChatScreen(
                    uiState = uiState,
                    userId = userChat.userId,
                    modifier = Modifier
                )
            }
        }
    }
}
