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
import kotlinx.coroutines.delay

// Configuración centralizada del NavHost
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash"
) {
    // Creamos los ViewModels compartidos
    val sharedRegisterViewModel: RegisterViewModel = viewModel()
    val sharedLoginViewModel: LoginViewModel = viewModel()
    
    // Observamos el estado de registro exitoso
    val isRegisterSuccessful by sharedRegisterViewModel.isRegisterSuccessful.collectAsState()
    
    // Observamos el estado de login exitoso
    val isLoginSuccessful by sharedLoginViewModel.isLoginSuccessful.collectAsState()
    
    // Efecto para navegar a Eventos cuando el login es exitoso
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            navController.navigate("eventos") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    
    // Efecto para navegar a Login cuando el registro es exitoso
    LaunchedEffect(isRegisterSuccessful) {
        if (isRegisterSuccessful) {
            // Esperamos un momento para que el usuario vea el mensaje de éxito
            delay(1500)
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            // Pantalla de splash que redirige a login después de un tiempo
            LaunchedEffect(key1 = true) {
                delay(2000)
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            
            // Contenido de la pantalla de splash
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Splash Screen",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
        
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                navController = navController,
                viewModel = sharedLoginViewModel
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
        
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo("forgot_password") { inclusive = true }
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
        
        // Ruta para la pantalla de eventos
        composable("eventos") {
            EventosScreen(
                onEventoClick = { evento ->
                    // Asumiendo que el campo ID en tu modelo Evento es "id"
                    navController.navigate("evento_detalle/${evento.id}")
                }
            )
        }
        
        // Ruta para el detalle de un evento
        composable(
            route = "evento_detalle/{eventoId}",
            arguments = listOf(
                navArgument("eventoId") { type = NavType.IntType }
            )
        ) {
            EventoDetailScreen(
                navController = navController
            )
        }
    }
}