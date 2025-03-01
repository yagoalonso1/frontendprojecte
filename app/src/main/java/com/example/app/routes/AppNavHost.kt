package com.example.app.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.app.view.HomeScreen
import com.example.app.viewmodel.LoginViewModel

// Configuración centralizada del NavHost
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Creamos los ViewModels compartidos
    val sharedRegisterViewModel: RegisterViewModel = viewModel()
    val sharedLoginViewModel: LoginViewModel = viewModel()
    
    // Observamos los estados usando collectAsState
    val isLoginSuccessful by sharedLoginViewModel.isLoginSuccessful.collectAsState()
    val isRegisterSuccessful by sharedRegisterViewModel.isRegisterSuccessful.collectAsState()
    
    // Efectos para manejar la navegación automática
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    
    LaunchedEffect(isRegisterSuccessful) {
        if (isRegisterSuccessful) {
            // Esperamos un momento para que el usuario vea el mensaje de éxito
            kotlinx.coroutines.delay(1500)
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
            // Reseteamos el estado para futuras navegaciones
            sharedRegisterViewModel.resetSuccessState()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = sharedLoginViewModel,
                navController = navController
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToOrganizador = { navController.navigate("register/organizador") },
                onNavigateToParticipante = { navController.navigate("register/participante") },
                viewModel = sharedRegisterViewModel
            )
        }

        composable("register/organizador") {
            OrganizadorScreen(
                viewModel = sharedRegisterViewModel
            )
        }

        composable("register/participante") {
            ParticipanteScreen(
                viewModel = sharedRegisterViewModel
            )
        }

        composable("forgot-password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo("forgot-password") { inclusive = true }
                }}
            )
        }
        
        composable("home") {
            HomeScreen(
                navController = navController,
                user = sharedLoginViewModel.user,
                viewModel = sharedLoginViewModel
            )
        }
    }
}