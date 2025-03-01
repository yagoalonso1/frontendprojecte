package com.example.app.routes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.view.LoginScreen
import com.example.app.view.register.RegisterScreen
import com.example.app.view.register.OrganizadorScreen
import com.example.app.view.register.ParticipanteScreen
import com.example.app.viewmodel.RegisterViewModel
import com.example.app.view.ForgotPasswordScreen

// Configuración centralizada del NavHost
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Creamos un único ViewModel compartido para todas las pantallas
    val sharedViewModel: RegisterViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                navController = navController
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToOrganizador = { navController.navigate("register/organizador") },
                onNavigateToParticipante = { navController.navigate("register/participante") },
                viewModel = sharedViewModel
            )
        }

        composable("register/organizador") {
            OrganizadorScreen(
                viewModel = sharedViewModel
            )
        }

        composable("register/participante") {
            ParticipanteScreen(
                viewModel = sharedViewModel
            )
        }

        composable("forgot-password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo("forgot-password") { inclusive = true }
                }}
            )
        }
    }
}