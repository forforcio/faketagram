package com.example.faketagram

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    val appContent: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = Screen.HOME.name,
            modifier = modifier
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw the screen first and place the bar on top.
            appContent(Modifier.fillMaxSize())

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
            ) {
                EditableBottomBar(
                    selectedRoute = route,
                    profileImageRes = uiState.getCurrentUserProfilePicture()
                        ?: R.drawable.default_user,
                    onScreenSelected = { screen ->
                        navController.navigate(screen.name) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                )
            }
        }
    } else {
        appContent(Modifier.fillMaxSize())
    }
}

@Composable
private fun EditableBottomBar(
    selectedRoute: String?,
    profileImageRes: Int,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.6f),
        modifier = Modifier
            .height(90.dp)
            .padding(0.dp)
    ) {
        Screen.entries.forEach { screen ->
            NavigationBarItem(
                selected = selectedRoute == screen.name,
                onClick = { onScreenSelected(screen) },
                icon = {
                    if (screen == Screen.PROFILE) {
                        Image(
                            painter = painterResource(profileImageRes),
                            contentDescription = "User photo",
                            modifier = Modifier
                                .clip(CircleShape)
                                .requiredSize(28.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        }
    }
}
