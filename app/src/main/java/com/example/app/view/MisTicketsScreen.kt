package com.example.app.view

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.AsyncImagePainter
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val successMessage = viewModel.successMessage
    
    // Colores consistentes con la app
    val primaryColor = Color(0xFFE53935)  // Rojo del logo
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    
    // Inicializar el calendarHelper
    val context = LocalContext.current
    
    // Estado para controlar el diálogo de permisos de calendario
    val showPermissionDialog = remember { mutableStateOf(false) }
    
    // Recordar la última compra seleccionada para calendario
    val selectedTicket = remember { mutableStateOf<TicketCompra?>(null) }
    
    // Coroutine scope para lanzar operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()
    
    // Launcher para permisos de calendario
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si tenemos permisos, intentamos añadir al calendario
            selectedTicket.value?.let { ticket ->
                coroutineScope.launch {
                    viewModel.addEventToCalendar(ticket)
                }
            }
        } else {
            // Si no tenemos permiso, mostramos un mensaje
            viewModel.setError("Se requieren permisos de calendario para esta función")
        }
        selectedTicket.value = null
    }
    
    LaunchedEffect(Unit) {
        viewModel.calendarHelper = GoogleCalendarHelper(context)
        
        // Intentar obtener la cuenta de Google del dispositivo
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            viewModel.googleAccount = account
            Log.d("MisTicketsScreen", "Cuenta Google detectada: ${account.email}")
        }
    }
    
    // Snackbar para mostrar mensajes de éxito
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(successMessage, errorMessage) {
        when {
            successMessage != null -> {
                snackbarHostState.showSnackbar(
                    message = successMessage,
                    duration = SnackbarDuration.Short,
                    actionLabel = "OK"
                )
            }
            errorMessage != null -> {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long,
                    actionLabel = "OK",
                    withDismissAction = true
                )
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                else -> TicketsContent(tickets, viewModel, primaryColor)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tienes tickets",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "¡Compra entradas para los eventos disponibles!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { navController.navigate("eventos") },
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            Text("Ver eventos disponibles")
        }
    }
}

@Composable
private fun TicketsContent(
    tickets: List<TicketCompra>,
    viewModel: TicketsViewModel,
    primaryColor: Color
) {
    // Estado para controlar el diálogo de permisos de calendario
    val showPermissionDialog = remember { mutableStateOf(false) }
    val showGoogleAccountDialog = remember { mutableStateOf(false) }
    val selectedTicket = remember { mutableStateOf<TicketCompra?>(null) }
    
    // Coroutine scope para lanzar operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()
    
    // Launcher para permisos de calendario
    val context = LocalContext.current
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si tenemos permisos, verificamos la cuenta de Google
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                viewModel.googleAccount = account
                // Añadir al calendario si tenemos permiso y cuenta
                selectedTicket.value?.let { ticket ->
                    coroutineScope.launch {
                        viewModel.addEventToCalendar(ticket)
                    }
                }
            } else {
                // No hay cuenta de Google, mostrar diálogo
                viewModel.googleAccount = null
                showGoogleAccountDialog.value = true
            }
        } else {
            // Si no tenemos permiso, mostramos un mensaje
            viewModel.setError("Se requieren permisos de calendario para esta función")
        }
        selectedTicket.value = null
    }
    
    // Mostrar diálogo de permisos si es necesario
    if (showPermissionDialog.value) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog.value = false },
            title = { Text("Permisos necesarios") },
            text = { 
                Column {
                    Text("Para añadir eventos al calendario, necesitas:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Tener iniciada sesión con Google en el dispositivo")
                    Text("2. Conceder permisos para acceder al calendario")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Esto permitirá a la aplicación crear eventos en tu Google Calendar.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    calendarPermissionLauncher.launch(android.Manifest.permission.WRITE_CALENDAR)
                    showPermissionDialog.value = false
                }) {
                    Text("Conceder permisos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para cuenta de Google no encontrada
    if (showGoogleAccountDialog.value) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog.value = false },
            title = { Text("Cuenta de Google requerida") },
            text = { 
                Column {
                    Text("No se ha encontrado una cuenta de Google válida en el dispositivo.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Para añadir eventos al calendario, necesitas iniciar sesión con una cuenta de Google en los ajustes del dispositivo.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Intentar abrir ajustes de cuentas
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_SYNC_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        viewModel.setError("No se pudieron abrir los ajustes de cuentas")
                    }
                    showGoogleAccountDialog.value = false
                }) {
                    Text("Abrir ajustes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAccountDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
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
                    // Este enfoque usa intents que funcionarán incluso sin una cuenta de Google establecida
                    coroutineScope.launch {
                        try {
                            // Verificar solo los permisos antes de proceder
                            val permission = android.Manifest.permission.WRITE_CALENDAR
                            when {
                                ContextCompat.checkSelfPermission(
                                    context, permission
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                                    // Ya tenemos permiso, proceder con añadir al calendario
                                    viewModel.addEventToCalendar(compra)
                                }
                                else -> {
                                    // Necesitamos solicitar permiso
                                    selectedTicket.value = compra
                                    showPermissionDialog.value = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MisTicketsScreen", "Error al intentar abrir intent de calendario: ${e.message}", e)
                            viewModel.setError("Error al abrir calendario: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TicketCompraItem(
    compra: TicketCompra,
    onItemClick: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    
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
            // Detalles del ticket
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                onClick = onAddToCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Añadir al calendario",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Añadir al calendario",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// Función auxiliar para formatear la fecha
private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        
        val outputFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es"))
        return outputFormat.format(date)
    } catch (e: Exception) {
        return dateString
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