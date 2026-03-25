package com.example.faketagram

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.faketagram.data.model.User
import com.example.faketagram.data.service.DataManagementService
import com.example.faketagram.data.service.ResourcesService
import com.example.faketagram.ui.StartChatScreen
import com.example.faketagram.ui.StartFeedScreen
import com.example.faketagram.ui.StartProfileScreen
import com.example.faketagram.ui.StartUserChatScreen
import com.example.faketagram.ui.model.UsersViewModel
import com.example.faketagram.ui.nav.Screen

@PreviewScreenSizes
@Composable
fun FaketagramApp(
    viewModel: UsersViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    var currentDestination by rememberSaveable { mutableStateOf(Screen.HOME) }

    val context = LocalContext.current
    val dataService = remember { DataManagementService() }
    val resourcesService = remember(context) { ResourcesService(context) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showNavBar = route == Screen.HOME.name ||
            route == Screen.CHAT.name ||
            route == Screen.PROFILE.name

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        context(resourcesService) {
            dataService.getUsersFromJson(R.raw.users)
        }
        context(dataService) {
            viewModel.init()
        }
    }
    
    val appContent: @Composable () -> Unit = {
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
                            it
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
                            it
                        )
                    },
                    modifier = Modifier
                )
            }
            composable<User> { backStackEntry ->
                val userChat: User = backStackEntry.toRoute()
                StartUserChatScreen(
                    uiState = uiState,
                    userId = userChat.userId,
                    modifier = Modifier,
                    onSendMessage = { text ->
                        viewModel.sendMessage(
                            receiverUid = userChat.firebaseUid,
                            text = text,
                        )
                    },
                    onSendPhoto = { uri ->
                        viewModel.onImageSelected(
                            receiverUid = userChat.firebaseUid,
                            uri = uri
                        )
                    }
                )
            }
            composable(route = Screen.PROFILE.name) {
                StartProfileScreen(
                    onLogoutButtonClicked = {
                        viewModel.logout()
                    },
                    modifier = Modifier
                )
            }
        }
    }

    if (showNavBar) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Screen.entries.forEach {
                    item(
                        icon = {
                            if (it == Screen.PROFILE) {
                                Image(
                                    painter = painterResource(
                                        uiState.getCurrentUserProfilePicture()?: R.drawable.default_user
                                    ),
                                    contentDescription = "User photo",
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .requiredSize(35.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else {
                                Icon(
                                    it.icon,
                                    contentDescription = it.label
                                )
                            }
                        },
                        selected = it == currentDestination,
                        onClick = {
                            currentDestination = it
                            navController.navigate(it.name)
                        }
                    )
                }
            },
            modifier = Modifier.imePadding(),
        ) {
            appContent()
        }
    } else {
        appContent()
    }
}
