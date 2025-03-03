package com.example.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app.util.formatDate
import com.example.app.viewmodel.EventoDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoDetailScreen(
    navController: NavController,
    viewModel: EventoDetailViewModel = viewModel()
) {
    val evento = viewModel.evento
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val errorMessage = viewModel.errorMessage
    
    // Colores consistentes con LoginScreen
    val primaryColor = Color(0xFFE53935)  // Rojo del logo
    val backgroundColor = Color.White
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.Gray
    val successColor = Color(0xFF4CAF50)  // Verde para elementos gratuitos
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Detalle del Evento",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else if (isError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error al cargar el evento",
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (evento != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Imagen del evento
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://eventflix.es/storage/${evento.imagen}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagen del evento",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Categoría
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(primaryColor.copy(alpha = 0.8f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = evento.categoria,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Contenido del evento
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Título
                            Text(
                                text = evento.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Información de fecha y hora
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Fecha
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Fecha",
                                            tint = primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = formatDate(evento.fechaEvento, true),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = textPrimaryColor
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Hora
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Hora",
                                            tint = primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = if (evento.hora.length >= 5) evento.hora.substring(0, 5) else evento.hora,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = textPrimaryColor
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Ubicación
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Ubicación",
                                            tint = primaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = evento.ubicacion,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = textPrimaryColor
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Descripción
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = evento.descripcion,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textSecondaryColor
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Precio
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (evento.precio > 0) 
                                        primaryColor.copy(alpha = 0.05f) 
                                    else 
                                        successColor.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Precio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimaryColor
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = if (evento.precio > 0) "${evento.precio}€" else "Gratuito",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (evento.precio > 0) primaryColor else successColor
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Botón de inscripción
                            Button(
                                onClick = { /* TODO: Implementar inscripción */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Inscribirse",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else {
                    // Caso en que evento es null pero no hay error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontró información del evento",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
} 