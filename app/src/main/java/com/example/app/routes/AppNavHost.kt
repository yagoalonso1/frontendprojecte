package com.example.app.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.view.*
import com.example.app.view.favoritos.FavoritosScreen
import com.example.app.view.register.OrganizadorScreen
import com.example.app.view.register.ParticipanteScreen
import com.example.app.view.register.RegisterScreen
import com.example.app.viewmodel.LoginViewModel
import com.example.app.viewmodel.ProfileViewModel
import com.example.app.viewmodel.RegisterViewModel
import com.example.app.viewmodel.TicketsViewModel
import kotlinx.coroutines.delay
import android.util.Log
import com.example.app.util.SessionManager

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = determineStartDestination()
) {
    val sharedRegisterViewModel: RegisterViewModel = viewModel()
    val sharedLoginViewModel: LoginViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    
    // Verificar token al inicio y en cada cambio de ruta
    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Lista de rutas que requieren autenticación
            val protectedRoutes = listOf(
                Routes.Eventos.route,
                Routes.MisEventos.route,
                Routes.Perfil.route,
                Routes.Favoritos.route
            )
            
            if (destination.route in protectedRoutes) {
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    Log.d("AppNavHost", "Ruta protegida sin token, navegando a Login")
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
    
    // Verificar token al inicio
    LaunchedEffect(Unit) {
        val token = SessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Log.d("AppNavHost", "No hay token válido, navegando a Login")
            navController.navigate(Routes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Observar estados de navegación
    val isRegisterSuccessful by sharedRegisterViewModel.isRegisterSuccessful.collectAsState()
    val isLogoutSuccessful by sharedLoginViewModel.isLogoutSuccessful.collectAsState()
    val shouldNavigateToLogin by profileViewModel.shouldNavigateToLogin.collectAsState()

    // Efecto para navegar después del registro exitoso
    LaunchedEffect(isRegisterSuccessful) {
        if (isRegisterSuccessful) {
            val userRole = SessionManager.getUserRole() ?: "participante"
            Log.d("AppNavHost", "Registro exitoso, rol en SessionManager: $userRole")
            
            delay(1500)
            navController.navigate(Routes.Eventos.route) {
                popUpTo(0) { inclusive = true }
            }
            sharedRegisterViewModel.resetSuccessState()
        }
    }

    // Efecto para manejar la navegación después del logout
    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin) {
            Log.d("AppNavHost", "Detectada señal de logout, redirigiendo a login")
            SessionManager.clearSession()
            navController.navigate(Routes.Login.route) {
                popUpTo(0) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            // Resetear el estado para evitar navegaciones repetidas
            profileViewModel.resetShouldNavigateToLogin()
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
                    Log.d("Navigation", "Clicked on evento with id: ${evento.id}")
                    val route = Routes.EventoDetalle.createRoute(evento.id.toString())
                    Log.d("Navigation", "Created route: $route")
                    try {
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                        Log.d("Navigation", "Successfully navigated to route: $route")
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error navigating to route: $route", e)
                    }
                },
                navController = navController
            )
        }
        
        composable(
            route = Routes.EventoDetalle.route,
            arguments = listOf(
                navArgument("eventoId") { 
                    type = NavType.StringType 
                    nullable = false
                    defaultValue = "-1"
                }
            )
        ) { backStackEntry ->
            Log.d("Navigation", "Entering EventoDetailScreen composition")
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: "-1"
            Log.d("Navigation", "EventoDetailScreen received eventoId: $eventoId")
            
            LaunchedEffect(eventoId) {
                Log.d("Navigation", "EventoDetailScreen LaunchedEffect triggered with eventoId: $eventoId")
            }
            
            EventoDetailScreen(
                navController = navController,
                eventoId = eventoId
            )
        }
        
        composable(Routes.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = sharedRegisterViewModel
            )
        }
        
        composable(Routes.RegisterOrganizador.route) {
            OrganizadorScreen(viewModel = sharedRegisterViewModel)
        }
        
        composable(Routes.RegisterParticipante.route) {
            ParticipanteScreen(viewModel = sharedRegisterViewModel)
        }

        // Rutas adicionales para el menú de navegación
        composable(Routes.MisEventos.route) {
            MisEventosScreen(
                navController = navController,
                onEventoClick = { evento ->
                    val route = Routes.EventoDetalle.createRoute(evento.id.toString())
                    navController.navigate(route)
                },
                onCreateEventoClick = {
                    navController.navigate(Routes.CrearEvento.route)
                }
            )
        }
        
        composable(Routes.CrearEvento.route) {
            CrearEventoScreen(navController = navController)
        }
        
        composable(Routes.MisTickets.route) {
            MisTicketsScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
        
        composable(Routes.Favoritos.route) {
            FavoritosScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
        
        composable(Routes.Perfil.route) {
            ProfileScreen(
                navController = navController
            )
        }
    }
}

// Función para determinar la ruta inicial basada en si hay token
private fun determineStartDestination(): String {
    val token = SessionManager.getToken()
    return if (token.isNullOrEmpty()) {
        Log.d("AppNavHost", "Iniciando en Login porque no hay token")
        Routes.Login.route
    } else {
        Log.d("AppNavHost", "Iniciando en Eventos porque hay token")
        Routes.Eventos.route
    }
} 