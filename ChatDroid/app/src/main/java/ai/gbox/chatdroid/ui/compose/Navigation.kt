package ai.gbox.chatdroid.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.gbox.chatdroid.network.ModelInfo

sealed class Screen(val route: String) {
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}/{chatTitle}") {
        fun createRoute(chatId: String, chatTitle: String) = "chat_detail/$chatId/$chatTitle"
    }
    object Settings : Screen("settings")
    object Admin : Screen("admin")
}

@Composable
fun ChatDroidNavigation(
    navController: NavHostController = rememberNavController(),
    availableModels: List<ModelInfo> = emptyList(),
    startDestination: String = Screen.ChatList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.ChatList.route) {
            ChatListComposeScreen(
                onChatClick = { chat ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(chat.id, chat.title)
                    )
                },
                onNewChatClick = {
                    // Create new chat and navigate to it
                    // This would need to be handled by a ViewModel
                }
            )
        }
        
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatTitle = backStackEntry.arguments?.getString("chatTitle") ?: "Chat"
            
            ChatComposeScreen(
                chatId = chatId,
                chatTitle = chatTitle
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                availableModels = availableModels,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Admin.route) {
            AdminScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController(),
    availableModels: List<ModelInfo> = emptyList()
) {
    Scaffold(
        topBar = {
            // Top app bar would be managed by individual screens
        },
        content = { paddingValues ->
            ChatDroidNavigation(
                navController = navController,
                availableModels = availableModels,
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}