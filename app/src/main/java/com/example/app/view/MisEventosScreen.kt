package com.example.app.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.viewmodel.EventoViewModel
import com.example.app.model.Evento
import com.example.app.routes.BottomNavigationBar
import com.example.app.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEventosScreen(
    navController: NavController,
    onEventoClick: (Evento) -> Unit,
    onCreateEventoClick: () -> Unit
) {
    val viewModel: EventoViewModel = viewModel()
    val eventos = viewModel.misEventos
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val errorMessage = viewModel.errorMessage
    
    // Verificar si el usuario es organizador
    val userRole = SessionManager.getUserRole() ?: "participante"
    val isOrganizador = userRole == "organizador"
    
    // Cargar mis eventos cuando se abre la pantalla, solo si es organizador
    LaunchedEffect(key1 = Unit) {
        if (isOrganizador) {
            viewModel.fetchMisEventos()
        }
    }

    // Colores consistentes con la app
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White

    Scaffold(
        // Barra superior con título y botón de crear
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "MIS EVENTOS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor
                    ) 
                },
                actions = {
                    // Botón para crear nuevo evento
                    IconButton(
                        onClick = onCreateEventoClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear Evento",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = primaryColor
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = userRole
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Pantalla de carga
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
                // Pantalla de error
                isError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = viewModel.errorMessage ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            
                            if (viewModel.errorMessage?.contains("Solo los organizadores") == true) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Esta sección está disponible solo para organizadores.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                // Lista de eventos vacía
                eventos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No has creado ningún evento todavía",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCreateEventoClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear mi primer evento")
                            }
                        }
                    }
                }
                // Lista de eventos
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(eventos) { evento ->
                            EventoCard(
                                evento = evento,
                                onClick = { onEventoClick(evento) },
                                primaryColor = primaryColor,
                                textPrimaryColor = Color.Black,
                                textSecondaryColor = Color.DarkGray,
                                successColor = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }
    }
}