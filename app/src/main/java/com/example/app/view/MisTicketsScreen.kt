package com.example.app.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.app.model.tickets.TicketCompra
import com.example.app.routes.BottomNavigationBar
import com.example.app.viewmodel.TicketsViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.app.util.getImageUrl
import com.example.app.util.GoogleCalendarHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTicketsScreen(
    navController: NavController,
    viewModel: TicketsViewModel = viewModel()
) {
    // Estados
    val tickets = viewModel.ticketsList
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    
    // Colores consistentes con la app
    val primaryColor = Color(0xFFE53935)  // Rojo del logo
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    
    // Inicializar el calendarHelper
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.calendarHelper = GoogleCalendarHelper(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "MIS TICKETS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = primaryColor
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadTickets() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = primaryColor
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = "participante"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            when {
                isLoading -> LoadingScreen(primaryColor)
                errorMessage != null -> ErrorScreen(errorMessage, primaryColor) { viewModel.loadTickets() }
                tickets.isEmpty() -> EmptyTicketsScreen(navController, primaryColor)
                else -> TicketsList(tickets, viewModel, primaryColor)
            }
        }
    }
}

@Composable
private fun LoadingScreen(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = primaryColor)
    }
}

@Composable
private fun ErrorScreen(errorMessage: String, primaryColor: Color, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Reintentar", color = Color.White)
        }
    }
}

@Composable
private fun EmptyTicketsScreen(navController: NavController, primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            tint = Color.Gray
        )
        Text(
            text = "No tienes tickets comprados",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Explora los eventos disponibles y compra tus entradas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("eventos") },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Ver eventos", color = Color.White)
        }
    }
}

@Composable
private fun TicketsList(
    tickets: List<TicketCompra>,
    viewModel: TicketsViewModel,
    primaryColor: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tickets) { compra ->
            TicketCompraItem(
                compra = compra,
                onItemClick = {
                    // Aquí podríamos navegar a los detalles del ticket
                    // En una futura implementación
                },
                onAddToCalendar = {
                    viewModel.addEventToCalendar(compra)
                }
            )
        }
    }
}

@Composable
fun TicketCompraItem(
    compra: TicketCompra,
    onItemClick: () -> Unit,
    onAddToCalendar: suspend () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    
    val showCalendarDialog = remember { mutableStateOf(false) }
    
    LaunchedEffect(showCalendarDialog.value) {
        if (showCalendarDialog.value) {
            onAddToCalendar()
            showCalendarDialog.value = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Imagen y detalles del evento
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del evento
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(grisClaro)
                ) {
                    if (compra.evento.imagen != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(compra.evento.imagen)
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Imagen del evento",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Detalles del evento
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = compra.evento.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(compra.evento.fecha),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Ver detalles",
                    tint = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón para añadir al calendario
            Button(
                onClick = { showCalendarDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Añadir al calendario",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

// Función para formatear la fecha en formato corto
fun formatDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// Función para formatear el precio
fun formatPrice(price: Double): String {
    return String.format(Locale.getDefault(), "%.2f", price)
} 