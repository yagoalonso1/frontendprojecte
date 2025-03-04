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
import com.example.app.view.favoritos.FavoritosScreen
import kotlinx.coroutines.delay

// Configuración centralizada del NavHost
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.Login.route
) {
    val sharedRegisterViewModel: RegisterViewModel = viewModel()
    val sharedLoginViewModel: LoginViewModel = viewModel()
    
    // Observar estados de navegación
    val isRegisterSuccessful by sharedRegisterViewModel.isRegisterSuccessful.collectAsState()
    val isLogoutSuccessful by sharedLoginViewModel.isLogoutSuccessful.collectAsState()

    // Efecto para navegar después del registro exitoso
    LaunchedEffect(isRegisterSuccessful) {
        if (isRegisterSuccessful) {
            delay(1500) // Dar tiempo para mostrar el mensaje de éxito
            navController.navigate(Routes.Eventos.route) {
                popUpTo(0) { inclusive = true }
            }
            sharedRegisterViewModel.resetSuccessState()
        }
    }

    // Efecto para navegar después del logout
    LaunchedEffect(isLogoutSuccessful) {
        if (isLogoutSuccessful) {
            navController.navigate(Routes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            sharedLoginViewModel.resetLogoutState()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Routes.ForgotPassword.route) },
                navController = navController,
                viewModel = sharedLoginViewModel
            )
        }
        
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = { 
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.Eventos.route) {
            EventosScreen(
                onEventoClick = { evento ->
                    navController.navigate(Routes.EventoDetalle.createRoute(evento.id.toString()))
                },
                navController = navController
            )
        }
        
        composable(
            route = Routes.EventoDetalle.route,
            arguments = listOf(
                navArgument("eventoId") { 
                    type = NavType.StringType 
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            EventoDetailScreen(
                navController = navController,
                eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            )
        }
        
        composable(Routes.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = sharedRegisterViewModel
            )
        }
        
        composable("register/organizador") {
            OrganizadorScreen(viewModel = sharedRegisterViewModel)
        }
        
        composable("register/participante") {
            ParticipanteScreen(viewModel = sharedRegisterViewModel)
        }

        // Rutas adicionales para el menú de navegación
        composable(Routes.MisEventos.route) {
            // Implementar pantalla de Mis Eventos
        }
        
        composable(Routes.CrearEvento.route) {
            // Implementar pantalla de Crear Evento
        }
        
        composable(Routes.MisTickets.route) {
            // Implementar pantalla de Mis Tickets
        }
        
        composable(Routes.Favoritos.route) {
            FavoritosScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
        
        composable(Routes.Perfil.route) {
            // Implementar pantalla de Perfil
        }
    }
} 