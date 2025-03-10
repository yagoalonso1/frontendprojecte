package com.example.app.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.app.view.EventoDetailScreen
import com.example.app.view.EventosScreen
import com.example.app.view.LoginScreen
import com.example.app.view.ForgotPasswordScreen

// Variable global para rastrear si el usuario está autenticado
private var isUserAuthenticated = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var userType by remember { mutableStateOf<UserType>(UserType.PARTICIPANT) }
    
    // Interceptar la navegación para manejar el inicio de sesión
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            // Si el usuario está autenticado y navega de vuelta a la pantalla de login, redirigirlo a eventos
            if (isUserAuthenticated && destination.route == Routes.Login.route) {
                controller.navigate(Routes.Eventos.route) {
                    popUpTo(Routes.Login.route) { inclusive = true }
                }
            }
        }
        
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (isUserAuthenticated) Routes.Eventos.route else Routes.Login.route
    ) {
        // Pantalla de login
        composable(Routes.Login.route) {
            LoginScreen(
                navController = navController,
                onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
            
            // Interceptar el evento de inicio de sesión exitoso
            navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("login_successful")?.let { success ->
                if (success) {
                    isUserAuthenticated = true
                    val role = navController.currentBackStackEntry?.savedStateHandle?.get<String>("user_role") ?: ""
                    userType = when (role) {
                        "organizador" -> UserType.ORGANIZER
                        "participante" -> UserType.PARTICIPANT
                        else -> UserType.PARTICIPANT
                    }
                    
                    // Navegar a la pantalla de eventos
                    navController.navigate(Routes.Eventos.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                    
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("login_successful")
                }
            }
        }
        
        // Pantalla de recuperación de contraseña
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Pantalla principal con menú de navegación
        composable(Routes.Eventos.route) {
            HomeScreenWithBottomNav(navController, userType)
        }
        
        // Pantalla de detalle de evento
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
    }
}

@Composable
fun HomeScreenWithBottomNav(navController: NavController, userType: UserType) {
    // Aquí implementa tu pantalla principal con el BottomNavigationBar
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = if (userType == UserType.ORGANIZER) "Organizador" else "Participante"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pantalla Principal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}