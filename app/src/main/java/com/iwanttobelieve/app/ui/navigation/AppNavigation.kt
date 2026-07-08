package com.iwanttobelieve.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.iwanttobelieve.app.ui.screens.auth.LoginScreen
import com.iwanttobelieve.app.ui.screens.auth.RegisterScreen
import com.iwanttobelieve.app.ui.screens.create.CreatePostScreen
import com.iwanttobelieve.app.ui.screens.feed.FeedScreen
import com.iwanttobelieve.app.ui.screens.main.MainScreen
import com.iwanttobelieve.app.ui.screens.profile.ProfileScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed"
    const val CREATE_POST = "create_post"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isLoggedIn) Routes.FEED else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // === Autenticação ===
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // === Tela Principal - Feed ===
        composable(Routes.FEED) {
            MainScreen(
                onNavigateToCreatePost = {
                    navController.navigate(Routes.CREATE_POST)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // === Criar Publicação ===
        composable(Routes.CREATE_POST) {
            CreatePostScreen(
                onPostCreated = { navController.popBackStack() }
            )
        }

        // === Perfil ===
        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}