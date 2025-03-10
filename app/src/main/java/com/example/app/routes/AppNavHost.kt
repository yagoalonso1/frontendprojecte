package com.example.app.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app.view.LoginScreen
import com.example.app.view.register.RegisterScreen
import com.example.app.view.register.OrganizadorScreen
import com.example.app.view.register.ParticipanteScreen
import com.example.app.viewmodel.RegisterViewModel
import com.example.app.view.ForgotPasswordScreen
import com.example.app.view.HomeScreen
import com.example.app.viewmodel.LoginViewModel
import com.example.app.view.EventosScreen
import com.example.app.view.EventoDetailScreen
import com.example.app.model.Evento
import kotlinx.coroutines.delay

// Configuración centralizada del NavHost
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "login"
) {
    // ViewModel compartido para el proceso de registro
    val sharedRegisterViewModel: RegisterViewModel = viewModel()
    val sharedLoginViewModel: LoginViewModel = viewModel()
    
    // Observamos el estado de cierre de sesión
    val isLogoutSuccessful by sharedLoginViewModel.isLogoutSuccessful.collectAsState()

    // Efecto para navegar a Login cuando el cierre de sesión es exitoso
    LaunchedEffect(isLogoutSuccessful) {
        if (isLogoutSuccessful) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            sharedLoginViewModel.resetLogoutState()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de inicio de sesión
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                navController = navController,
                viewModel = sharedLoginViewModel
            )
        }
        
        // Pantalla de recuperación de contraseña
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        
        // Pantalla principal
        composable("home") {
            HomeScreen(
                user = sharedLoginViewModel.user,
                viewModel = sharedLoginViewModel
            )
        }
        
        // Pantalla de eventos
        composable("eventos") {
            EventosScreen(
                onEventoClick = { evento: Evento ->
                    navController.navigate("evento_detalle/${evento.id}")
                },
                navController = navController
            )
        }
        
        // Ruta para el detalle de un evento
        composable(
            route = "evento_detalle/{eventoId}",
            arguments = listOf(
                navArgument("eventoId") { type = NavType.IntType }
            )
        ) {
            EventoDetailScreen()
        }
        
        // Pantalla de registro
        composable("register") {
            RegisterScreen(
                navController = navController,
                viewModel = sharedRegisterViewModel
            )
        }
        
        // Pantalla de registro para organizador
        composable("register/organizador") {
            OrganizadorScreen(
                viewModel = sharedRegisterViewModel
            )
        }
        
        // Pantalla de registro para participante
        composable("register/participante") {
            ParticipanteScreen(
                viewModel = sharedRegisterViewModel
            )
        }
    }
}