package com.example.app.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.example.app.view.MisEventosScreen
import com.example.app.util.SessionManager
import android.util.Log
import com.example.app.view.CrearEventoScreen

// Variable global para rastrear si el usuario está autenticado
private var isUserAuthenticated = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Estado para el rol
    var currentRole by remember { 
        mutableStateOf(SessionManager.getUserRole() ?: "participante") 
    }
    
    // Efecto para actualizar el rol cuando la composición se inicie
    LaunchedEffect(Unit) {
        val role = SessionManager.getUserRole()
        Log.d("AppNavigation", "LaunchedEffect - Rol inicial: $role")
        if (role != null) {
            currentRole = role
            Log.d("AppNavigation", "LaunchedEffect - currentRole actualizado a: $currentRole")
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
                    val role = SessionManager.getUserRole() ?: ""
                    Log.d("AppNavigation", "Login exitoso - Rol obtenido: $role")
                    currentRole = role
                    Log.d("AppNavigation", "Login exitoso - currentRole actualizado a: $currentRole")
                    
                    // Navegar a la pantalla de eventos
                    navController.navigate(Routes.Eventos.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                    
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("login_successful")
                }
            }
        }
        
        // Pantalla principal con menú de navegación
        composable(Routes.Eventos.route) {
            Log.d("AppNavigation", "Composable Eventos - currentRole: $currentRole")
            HomeScreenWithBottomNav(
                navController = navController,
                userRole = currentRole
            )
        }
        
        composable(Routes.CrearEvento.route) {
            CrearEventoScreen(navController = navController)
        }
    }
}

@Composable
fun HomeScreenWithBottomNav(
    navController: NavController,
    userRole: String
) {
    Log.d("HomeScreenWithBottomNav", "Iniciando con userRole: $userRole")
    
    Scaffold(
        bottomBar = {
            Log.d("HomeScreenWithBottomNav", "Construyendo BottomBar con userRole: $userRole")
            BottomNavigationBar(
                navController = navController,
                userRole = userRole
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            EventosScreen(
                onEventoClick = { evento ->
                    val route = Routes.EventoDetalle.createRoute(evento.id.toString())
                    navController.navigate(route)
                },
                navController = navController
            )
        }
    }
}