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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.app.model.tickets.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import com.example.app.R
import com.example.app.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTicketsScreen(
    navController: NavController,
    viewModel: TicketsViewModel = viewModel(
        factory = TicketsViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    // Estados
    val tickets by viewModel.tickets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val downloadMessage by viewModel.downloadMessage.collectAsState()
    
    // Colores consistentes con la app
    val primaryColor = Color(0xFFE53935)  // Rojo del logo
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    val successColor = Color(0xFF4CAF50)  // Verde para mensajes de éxito
    
    // Inicializar el calendarHelper
    val context = LocalContext.current
    
    // Estado para controlar el diálogo de permisos de calendario
    val showPermissionDialog = remember { mutableStateOf(false) }
    
    // Recordar la última compra seleccionada para calendario
    val selectedTicket = remember { mutableStateOf<TicketCompra?>(null) }
    
    // Coroutine scope para lanzar operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()
    
    // Snackbar para mostrar mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para mostrar Snackbar cuando se complete la descarga
    LaunchedEffect(downloadMessage) {
        if (downloadMessage == "Entrada descargada correctamente") {
            snackbarHostState.showSnackbar(
                message = "¡Entrada descargada correctamente!",
                actionLabel = "Ver",
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        } else if (downloadMessage?.startsWith("Error") == true) {
            snackbarHostState.showSnackbar(
                message = downloadMessage ?: "Error al descargar",
                actionLabel = "OK",
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }
    
    // Efecto para mostrar mensajes de error
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long,
                actionLabel = "OK",
                withDismissAction = true
            )
        }
    }
    
    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(
                            onClick = { data.dismiss() }
                        ) {
                            Text(
                                text = data.visuals.actionLabel ?: "OK",
                                color = Color.White
                            )
                        }
                    },
                    containerColor = if (data.visuals.message.startsWith("¡")) successColor else primaryColor,
                    contentColor = Color.White,
                    dismissAction = if (data.visuals.withDismissAction) {
                        {
                            IconButton(onClick = { data.dismiss() }) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Cerrar", 
                                    tint = Color.White
                                )
                            }
                        }
                    } else null
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.mis_tickets_titulo),
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
                error != null -> ErrorScreen(error!!, primaryColor) { viewModel.loadTickets() }
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
            Text(stringResource(id = R.string.mis_tickets_reintentar), color = Color.White)
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
            text = stringResource(id = R.string.mis_tickets_no_tickets),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(id = R.string.mis_tickets_compra_entradas),
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
            Text(stringResource(id = R.string.mis_tickets_ver_eventos_disponibles))
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
            viewModel.setError("¡Permiso de calendario requerido!")
        }
        selectedTicket.value = null
    }
    
    // Mostrar diálogo de permisos si es necesario
    if (showPermissionDialog.value) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog.value = false },
            title = { Text("¡Permisos necesarios!") },
            text = { 
                Column {
                    Text("¡Permiso de calendario desc!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¡Permiso de calendario req!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¡Permiso de calendario perm!")
                }
            },
            confirmButton = {
                Button(onClick = {
                    calendarPermissionLauncher.launch(android.Manifest.permission.WRITE_CALENDAR)
                    showPermissionDialog.value = false
                }) {
                    Text("¡Conceder permisos!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog.value = false }) {
                    Text("¡Cancelar!")
                }
            }
        )
    }
    
    // Diálogo para cuenta de Google no encontrada
    if (showGoogleAccountDialog.value) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog.value = false },
            title = { Text("¡Cuenta de Google requerida!") },
            text = { 
                Column {
                    Text("¡Cuenta de Google no encontrada!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¡Permiso de Google perm!")
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
                        viewModel.setError("¡Error al abrir ajustes de cuentas!")
                    }
                    showGoogleAccountDialog.value = false
                }) {
                    Text("¡Abrir ajustes!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAccountDialog.value = false }) {
                    Text("¡Cancelar!")
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
                                    // Intentar obtener la Activity
                                    val activity = context as? androidx.activity.ComponentActivity
                                    if (activity != null) {
                                        viewModel.addEventToCalendar(compra, activity)
                                    } else {
                                        // Usar el método alternativo si no es una FragmentActivity
                                        viewModel.addEventToCalendar(compra)
                                    }
                                }
                                else -> {
                                    // Necesitamos solicitar permiso
                                    selectedTicket.value = compra
                                    showPermissionDialog.value = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MisTicketsScreen", "Error al intentar abrir intent de calendario: ${e.message}", e)
                            viewModel.setError("¡Error al intentar abrir intent de calendario!")
                        }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun TicketCompraItem(
    compra: TicketCompra,
    onItemClick: () -> Unit,
    onAddToCalendar: () -> Unit,
    viewModel: TicketsViewModel
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val grisClaro = Color(0xFFF5F5F5)
    
    // Estados para la descarga
    val isDownloading by viewModel.isDownloadingPdf.collectAsState()
    val downloadMessage by viewModel.downloadMessage.collectAsState()
    
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
            
            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para añadir al calendario
                Button(
                    onClick = onAddToCalendar,
                    modifier = Modifier
                        .weight(1f)
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
                        text = stringResource(id = R.string.mis_tickets_calendario),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                
                // Botón para descargar entrada
                Button(
                    onClick = { 
                        compra.tickets.firstOrNull()?.let { ticket ->
                            viewModel.downloadEntrada(ticket.id)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDownloading) Color(0xFFE0E0E0) else Color(0xFF4CAF50),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isDownloading && downloadMessage != "¡Entrada descargada correctamente!"
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = downloadMessage ?: "¡Descargando entrada!",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        } else if (downloadMessage == "¡Entrada descargada correctamente!") {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "¡Entrada descargada!",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Entrada descargada!",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "¡Descargar entrada!",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Descargar entrada!",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para formatear la fecha
private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        
        // Obtener el idioma actual del usuario
        val userLanguage = SessionManager.getUserLanguage() ?: "es"
        
        // Definir el patrón según el idioma
        val pattern = when (userLanguage) {
            "en" -> "MMMM d, yyyy"
            "ca", "es" -> "d 'de' MMMM, yyyy"
            else -> "d 'de' MMMM, yyyy" // Por defecto, español
        }
        
        // Obtener el Locale correspondiente
        val locale = when (userLanguage) {
            "en" -> Locale.ENGLISH
            "ca" -> Locale("ca", "ES")
            "es" -> Locale("es", "ES")
            else -> Locale("es", "ES") // Español por defecto
        }
        
        val outputFormat = SimpleDateFormat(pattern, locale)
        return outputFormat.format(date)
    } catch (e: Exception) {
        return dateString
    }
}

// Función para formatear la fecha en formato corto
fun formatDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        // Obtener el idioma actual del usuario
        val userLanguage = SessionManager.getUserLanguage() ?: "es"
        
        // Obtener el Locale correspondiente
        val locale = when (userLanguage) {
            "en" -> Locale.ENGLISH
            "ca" -> Locale("ca", "ES")
            "es" -> Locale("es", "ES")
            else -> Locale("es", "ES") // Español por defecto
        }
        
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", locale)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// Función para formatear el precio
fun formatPrice(price: Double): String {
    // Obtener el idioma actual del usuario
    val userLanguage = SessionManager.getUserLanguage() ?: "es"
    
    // Obtener el Locale correspondiente
    val locale = when (userLanguage) {
        "en" -> Locale.US
        "ca" -> Locale("ca", "ES")
        "es" -> Locale("es", "ES")
        else -> Locale("es", "ES") // Español por defecto
    }
    
    return String.format(locale, "%.2f", price)
}

@Composable
fun TicketCard(
    ticket: Ticket,
    onTicketClick: (Ticket) -> Unit,
    viewModel: TicketsViewModel = viewModel(
        factory = TicketsViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val isDownloading by viewModel.isDownloadingPdf.collectAsState()
    val downloadMessage by viewModel.downloadMessage.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTicketClick(ticket) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ... resto del código existente ...

            // Botón de descarga de entrada
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { viewModel.downloadEntrada(ticket.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isDownloading && downloadMessage != "¡Entrada descargada correctamente!"
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡Descargando entrada!",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    } else if (downloadMessage == "¡Entrada descargada correctamente!") {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡Entrada descargada!",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

class TicketsViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TicketsViewModel::class.java)) {
            return TicketsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 