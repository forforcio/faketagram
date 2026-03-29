package com.izzo.meelt

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.izzo.meelt.data.model.Chat
import com.izzo.meelt.data.service.DataManagementService
import com.izzo.meelt.data.service.ResourcesService
import com.izzo.meelt.ui.nav.StartBlockedUserScreen
import com.izzo.meelt.ui.nav.StartChatScreen
import com.izzo.meelt.ui.nav.StartFeedScreen
import com.izzo.meelt.ui.nav.StartProfileScreen
import com.izzo.meelt.ui.nav.StartUserChatScreen
import com.izzo.meelt.ui.nav.StartUserProfileScreen
import com.izzo.meelt.ui.model.UsersViewModel
import com.izzo.meelt.ui.nav.Screen
import com.izzo.meelt.ui.nav.UserChatRoute
import com.izzo.meelt.ui.nav.UserProfileRoute

@PreviewScreenSizes
@Composable
fun MeeltApp(
    viewModel: UsersViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    initialChatUserId: Int? = null,
    onInitialChatConsumed: () -> Unit = {},
    onLogoutRequested: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataService = remember { DataManagementService() }
    val resourcesService = remember(context) { ResourcesService(context) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val isUserProfileRoute =
        route?.contains(UserProfileRoute::class.qualifiedName.orEmpty()) == true
    val isUserChatRoute = route?.contains(UserChatRoute::class.qualifiedName.orEmpty()) == true

    val uiState by viewModel.uiState.collectAsState()

    // Show nav bar on chat route only when user is blocked (shows blocked screen, not chat)
    val blockedChatUserId = if (isUserChatRoute) {
        try {
            backStackEntry?.toRoute<UserChatRoute>()?.userId
        } catch (_: Exception) {
            null
        }
    } else null
    val isChatRouteBlocked = blockedChatUserId != null && uiState.isUserBlocked(blockedChatUserId)

    val showNavBar = route == Screen.HOME.name ||
            route == Screen.CHAT.name ||
            route == Screen.PROFILE.name ||
            isUserProfileRoute ||
            isChatRouteBlocked

    LaunchedEffect(Unit) {
        context(dataService, resourcesService, context.applicationContext) {
            viewModel.loadUiStateContents()
        }
    }

    LaunchedEffect(initialChatUserId) {
        val userId = initialChatUserId ?: return@LaunchedEffect
        navController.navigate(UserChatRoute(userId)) {
            launchSingleTop = true
        }
        onInitialChatConsumed()
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
                    onUserPhotoClicked = { user ->
                        navController.navigate(
                            UserProfileRoute(user.userId)
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            composable(route = Screen.CHAT.name) {
                StartChatScreen(
                    uiState = uiState,
                    onUserClick = { user ->
                        navController.navigate(
                            UserChatRoute(user.userId)
                        )
                    },
                    modifier = Modifier
                )
            }
            composable<UserProfileRoute> { backStackEntry ->
                val profileRoute: UserProfileRoute = backStackEntry.toRoute()
                val selectedUser = uiState.getUserById(profileRoute.userId)

                if (uiState.isUserBlocked(selectedUser.userId)) {
                    StartBlockedUserScreen(
                        user = selectedUser,
                        modifier = Modifier.fillMaxSize(),
                        onUnblock = { viewModel.unblockUser(it.userId) }
                    )
                } else {
                    StartUserProfileScreen(
                        user = selectedUser,
                        modifier = Modifier.fillMaxSize(),
                        onChatClick = {
                            navController.navigate(UserChatRoute(selectedUser.userId))
                        },
                        onBlockClick = { viewModel.blockUser(it.userId) }
                    )
                }
            }
            composable<UserChatRoute> { backStackEntry ->
                val chatRoute: UserChatRoute = backStackEntry.toRoute()
                val userChat: Chat = uiState.getChatByUserId(chatRoute.userId)

                if (uiState.isUserBlocked(userChat.interlocutor.userId)) {
                    StartBlockedUserScreen(
                        user = userChat.interlocutor,
                        modifier = Modifier.fillMaxSize(),
                        onUnblock = { viewModel.unblockUser(it.userId) }
                    )
                } else {
                    StartUserChatScreen(
                        uiState = uiState,
                        chat = userChat,
                        modifier = Modifier,
                        onSendMessage = { text ->
                            viewModel.sendMessage(
                                receiverUid = userChat.interlocutor.firebaseUid,
                                text = text,
                            )
                        },
                        onSendPhoto = { uri ->
                            viewModel.onImageSelected(
                                receiverUid = userChat.interlocutor.firebaseUid,
                                uri = uri
                            )
                        },
                        onChatOpened = {
                            viewModel.setActiveChatUserId(it)
                            viewModel.setAllMessagesAsReadByUser(it)
                        },
                        onChatClosed = { viewModel.clearActiveChatUserId() },
                        deleteMessage = { viewModel.deleteMessage(it) }
                    )
                }
            }
            composable(route = Screen.PROFILE.name) {
                StartProfileScreen(
                    availableUsersJsonNames = uiState.availableUsersJsonNames,
                    selectedUsersJsonName = uiState.selectedUsersJsonName,
                    onUsersJsonSelected = { jsonName ->
                        context(dataService, resourcesService, context.applicationContext) {
                            viewModel.selectUsersJson(jsonName)
                        }
                    },
                    onLogoutButtonClicked = {
                        viewModel.logout()
                        onLogoutRequested()
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
                            contentDescription = stringResource(R.string.content_desc_user_photo),
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
                            contentDescription = when (screen) {
                                Screen.HOME -> stringResource(R.string.nav_home)
                                Screen.CHAT -> stringResource(R.string.nav_chat)
                                Screen.PROFILE -> stringResource(R.string.nav_profile)
                            },
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        }
    }
}
