package com.example.app.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.app.routes.BottomNavigationBar
import com.example.app.viewmodel.CompraItem
import com.example.app.viewmodel.HistorialComprasViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Colores consistentes con la app
private val primaryColor = Color(0xFFE53935)  // Rojo del logo
private val backgroundColor = Color.White
private val textPrimaryColor = Color.Black
private val textSecondaryColor = Color.DarkGray
private val surfaceColor = Color(0xFFF5F5F5)  // Gris muy claro para fondos
private val successColor = Color(0xFF4CAF50)  // Verde para estados exitosos
private val warningColor = Color(0xFFFFA000)  // Ámbar para estados pendientes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialComprasScreen(
    navController: NavController,
    viewModel: HistorialComprasViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HistorialComprasViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val compras by viewModel.compras.collectAsState()
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val shouldNavigateToLogin by viewModel.shouldNavigateToLogin.collectAsState()
    
    // Navegar al login cuando la sesión ha expirada
    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin) {
            Log.d("HistorialComprasScreen", "Sesión expirada, redirigiendo a login")
            com.example.app.util.SessionManager.clearSession()
            
            navController.navigate(com.example.app.routes.Routes.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            
            viewModel.resetShouldNavigateToLogin()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "HISTORIAL DE COMPRAS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
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
            // Mostrar la barra de navegación inferior consistente con otras pantallas
            BottomNavigationBar(
                navController = navController,
                userRole = com.example.app.util.SessionManager.getUserRole() ?: "participante"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = backgroundColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage, 
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { viewModel.loadHistorialCompras() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Reintentar", color = Color.White)
                    }
                }
            } else if (compras.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp),
                        tint = Color.LightGray
                    )
                    
                    Text(
                        text = "No tienes compras realizadas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cuando realices una compra, aparecerá aquí",
                        fontSize = 16.sp,
                        color = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { navController.navigate(com.example.app.routes.Routes.Eventos.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Explorar eventos", color = Color.White)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(compras) { compra ->
                        CompraCard(
                            compra = compra,
                            onCompraClick = { /* TODO: Implementar detalle de compra */ },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompraCard(
    compra: CompraItem,
    onCompraClick: (CompraItem) -> Unit,
    viewModel: HistorialComprasViewModel = viewModel()
) {
    val isDownloading by viewModel.isDownloadingPdf.collectAsState()
    val downloadMessage by viewModel.downloadMessage.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCompraClick(compra) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Información del evento
            compra.evento?.let { evento ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del evento
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        if (evento.imagen != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(evento.imagen)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = evento.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = evento.nombre.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = evento.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = textSecondaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = formatFechaSinYear(evento.fecha) + " - " + evento.hora,
                                fontSize = 14.sp,
                                color = textSecondaryColor
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
            
            // Entradas y total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${compra.entradas.size} entradas",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = textPrimaryColor
                )
                
                // Precio total
                Text(
                    text = formatCurrency(compra.total),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fecha de compra y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = textSecondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Compra: ${formatFechaSinYear(compra.fecha_compra)}",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
                
                // Chip para el estado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (compra.estado.lowercase()) {
                        "pagado" -> successColor.copy(alpha = 0.1f)
                        "pendiente" -> warningColor.copy(alpha = 0.1f)
                        else -> Color.LightGray.copy(alpha = 0.3f)
                    },
                    contentColor = when (compra.estado.lowercase()) {
                        "pagado" -> successColor
                        "pendiente" -> warningColor
                        else -> textSecondaryColor
                    },
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Text(
                        text = compra.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Añadir botón de descarga de factura
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { viewModel.downloadFactura(compra.id_compra) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isDownloading && downloadMessage != "Factura descargada correctamente"
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
                            text = "DESCARGANDO...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    } else if (downloadMessage == "Factura descargada correctamente") {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FACTURA DESCARGADA",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Descargar factura",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DESCARGAR FACTURA",
                            color = Color.White,
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

// Función para formatear moneda
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    format.currency = Currency.getInstance("EUR")
    return format.format(amount)
}

// Función para formatear fecha con nombre diferente para evitar ambigüedad
private fun formatFechaSinYear(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString.split(" ")[0])
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        Log.e("HistorialComprasScreen", "Error al formatear fecha: $e")
        dateString
    }
}

class HistorialComprasViewModelFactory(
    private val application: android.app.Application
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistorialComprasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistorialComprasViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 