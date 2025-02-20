package com.example.app.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.view.LoginScreen
import com.example.app.view.RegisterScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            // Pantalla de Login
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Registro
        composable(Routes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }
    }
}