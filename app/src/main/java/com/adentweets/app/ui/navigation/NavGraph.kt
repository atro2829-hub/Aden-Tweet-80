package com.adentweets.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adentweets.app.ui.screens.auth.SplashScreen
import com.adentweets.app.ui.screens.auth.LoginScreen
import com.adentweets.app.ui.screens.auth.RegisterScreen
import com.adentweets.app.ui.screens.auth.ForgotPasswordScreen
import com.adentweets.app.ui.screens.home.HomeScreen
import com.adentweets.app.ui.screens.explore.ExploreScreen
import com.adentweets.app.ui.screens.notifications.NotificationsScreen
import com.adentweets.app.ui.screens.messages.MessagesScreen
import com.adentweets.app.ui.screens.messages.ChatScreen
import com.adentweets.app.ui.screens.compose.ComposeScreen
import com.adentweets.app.ui.screens.tweet.TweetDetailScreen
import com.adentweets.app.ui.screens.profile.ProfileScreen
import com.adentweets.app.ui.screens.profile.EditProfileScreen
import com.adentweets.app.ui.screens.profile.FollowersScreen
import com.adentweets.app.ui.screens.profile.FollowingScreen
import com.adentweets.app.ui.screens.bookmarks.BookmarksScreen
import com.adentweets.app.ui.screens.lists.ListsScreen
import com.adentweets.app.ui.screens.settings.SettingsScreen
import com.adentweets.app.ui.screens.settings.AboutScreen
import com.adentweets.app.ui.screens.settings.NotificationSettingsScreen
import com.adentweets.app.ui.screens.settings.PrivacySettingsScreen
import com.adentweets.app.ui.screens.settings.AppearanceSettingsScreen
import com.adentweets.app.ui.screens.settings.AccountSettingsScreen
import com.adentweets.app.ui.viewmodel.AuthViewModel
import com.adentweets.app.util.Constants

@Composable
fun AdenTweetNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = Constants.Routes.SPLASH) {
        composable(Constants.Routes.SPLASH) {
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn) {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.SPLASH) { inclusive = true }
                    }
                }
            }
            SplashScreen()
        }

        composable(Constants.Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Constants.Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Constants.Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.LOGIN) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Constants.Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Constants.Routes.LOGIN) },
                onRegisterSuccess = {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.REGISTER) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Constants.Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(Constants.Routes.HOME) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Constants.Routes.EXPLORE) {
            ExploreScreen(navController = navController)
        }

        composable(Constants.Routes.NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }

        composable(Constants.Routes.MESSAGES) {
            MessagesScreen(navController = navController)
        }

        composable(
            route = Constants.Routes.CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                navController = navController
            )
        }

        composable(Constants.Routes.COMPOSE) { entry ->
            val replyToId = entry.savedStateHandle.getString("replyToId")
            val replyToAuthor = entry.savedStateHandle.getString("replyToAuthor") ?: ""
            ComposeScreen(
                navController = navController,
                replyToId = replyToId,
                replyToAuthor = replyToAuthor
            )
        }

        composable(
            route = Constants.Routes.TWEET_DETAIL,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            TweetDetailScreen(
                postId = postId,
                navController = navController
            )
        }

        composable(Constants.Routes.PROFILE) {
            ProfileScreen(
                navController = navController,
                isOwnProfile = true
            )
        }

        composable(
            route = Constants.Routes.USER_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(
                navController = navController,
                isOwnProfile = false,
                userId = userId
            )
        }

        composable(Constants.Routes.EDIT_PROFILE) {
            EditProfileScreen(navController = navController)
        }

        composable(
            route = Constants.Routes.FOLLOWERS,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            FollowersScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(
            route = Constants.Routes.FOLLOWING,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            FollowingScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(Constants.Routes.BOOKMARKS) {
            BookmarksScreen(navController = navController)
        }

        composable(Constants.Routes.LISTS) {
            ListsScreen(navController = navController)
        }

        composable(Constants.Routes.SETTINGS) {
            SettingsScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Constants.Routes.ABOUT) {
            AboutScreen(navController = navController)
        }

        composable(Constants.Routes.NOTIF_SETTINGS) {
            NotificationSettingsScreen(navController = navController)
        }

        composable(Constants.Routes.PRIVACY_SETTINGS) {
            PrivacySettingsScreen(navController = navController)
        }

        composable(Constants.Routes.APPEARANCE_SETTINGS) {
            AppearanceSettingsScreen(navController = navController)
        }

        composable(Constants.Routes.ACCOUNT_SETTINGS) {
            AccountSettingsScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}