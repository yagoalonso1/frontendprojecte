package com.example.app.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.model.User
import com.example.app.viewmodel.LoginViewModel

@Composable
fun HomeScreen(
    user: User?,
    viewModel: LoginViewModel,
    navController: NavController
) {
    val isLogoutSuccessful by viewModel.isLogoutSuccessful.collectAsState()
    
    LaunchedEffect(isLogoutSuccessful) {
        if (isLogoutSuccessful) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            viewModel.resetLogoutState()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Bienvenido!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Has iniciado sesión como ${user?.email ?: "Usuario"}",
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Rol: ${user?.role ?: "No especificado"}",
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.performLogout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                )
            ) {
                Text("Cerrar sesión")
            }
        }
    }
} 