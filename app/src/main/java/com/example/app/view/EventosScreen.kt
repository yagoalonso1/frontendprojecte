package com.example.app.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app.R
import com.example.app.model.Evento
import com.example.app.util.formatDate
import com.example.app.util.CategoryTranslator
import com.example.app.viewmodel.EventoViewModel
import kotlinx.coroutines.launch
import com.example.app.routes.BottomNavigationBar
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.util.Log
import com.example.app.util.SessionManager
import com.example.app.util.Constants
import com.example.app.util.getImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    onEventoClick: (Evento) -> Unit,
    navController: NavController,
    viewModel: EventoViewModel = viewModel()
) {
    val eventos = viewModel.eventos
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val errorMessage = viewModel.errorMessage
    val userRole = SessionManager.getUserRole() ?: "participante"
    
    // Actualizar eventos cada vez que se muestra la pantalla
    LaunchedEffect(Unit) {
        Log.d("EventosScreen", "Actualizando lista de eventos...")
        viewModel.fetchEventos()
    }
    
    Log.d("EventosScreen", "Rol del usuario: $userRole")

    // Colores consistentes con la app
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.DarkGray
    val successColor = Color(0xFF4CAF50)

    // Estado para el texto de búsqueda
    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Filtrar eventos basados en el texto de búsqueda
    val filteredEventos = remember(eventos, searchText) {
        if (searchText.isEmpty()) {
            eventos
        } else {
            eventos.filter { evento ->
                evento.titulo.contains(searchText, ignoreCase = true) ||
                evento.descripcion.contains(searchText, ignoreCase = true) ||
                evento.categoria.contains(searchText, ignoreCase = true)
            }
        }
    }

    // Estado para el botón de scroll hacia arriba
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    // Modificar la lógica de visibilidad de la barra de búsqueda
    val showSearchBar by remember {
        derivedStateOf {
            showScrollToTop || searchText.isNotEmpty()
        }
    }

    Scaffold(
        // Barra superior
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.eventos_titulo),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor,
                        modifier = Modifier.padding(start = 8.dp)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,  // Fondo blanco para la barra superior
                    titleContentColor = primaryColor
                )
            )
        },
        // Barra de navegación inferior
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
            // Pantalla de carga
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
            } 
            // Pantalla de error
            else if (isError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: stringResource(id = R.string.eventos_error_desconocido),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            } 
            // Lista de eventos
            else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Barra de búsqueda
                    AnimatedVisibility(
                        visible = showSearchBar,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                placeholder = { Text(stringResource(id = R.string.eventos_buscar)) },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchText.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchText = "" }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(id = R.string.eventos_limpiar_busqueda),
                                                tint = Color(0xFFE53935) // Color rojo del logo
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE53935),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedLabelColor = Color(0xFFE53935),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )
                        }
                    }
                    
                    // Lista de eventos
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredEventos) { evento ->
                            EventoCard(
                                evento = evento,
                                onClick = { onEventoClick(evento) },
                                primaryColor = primaryColor,
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor,
                                successColor = successColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventoCard(
    evento: Evento,
    onClick: () -> Unit,
    primaryColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    successColor: Color
) {
    // Usar la función de extensión para obtener la URL de la imagen
    val imageUrl = evento.getImageUrl()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen del evento
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(id = R.string.eventos_imagen),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            
            // Información del evento
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Categoría traducida
                val categoriaTraducida = CategoryTranslator.translate(evento.categoria)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoriaTraducida,
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Título del evento
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textPrimaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(id = R.string.eventos_fecha),
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = formatDate(evento.fechaEvento, true),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryColor
                        )
                    }
                    
                    // Hora
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = stringResource(id = R.string.eventos_hora),
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Asegurar que la hora siempre tenga formato HH:MM
                        val formattedHora = if (evento.hora.contains(":")) {
                            val parts = evento.hora.split(":")
                            val hours = parts[0].padStart(2, '0')
                            val minutes = if (parts.size > 1) parts[1].padStart(2, '0') else "00"
                            "$hours:$minutes"
                        } else {
                            evento.hora.padStart(2, '0') + ":00"
                        }
                        
                        Text(
                            text = formattedHora,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(id = com.example.app.R.string.eventos_ubicacion),
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = evento.ubicacion,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textSecondaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Precio
                val entradas = evento.entradas ?: emptyList()
                
                // Solo registramos en el log si la lista no es null
                if (entradas.isNotEmpty()) {
                    Log.d("EventosCard", "Evento ${evento.titulo} - Entradas: ${entradas.size}")
                    entradas.forEachIndexed { index, entrada ->
                        Log.d("EventosCard", "  Entrada[$index]: ${entrada.nombre}, " +
                              "esIlimitado=${entrada.esIlimitado}, " +
                              "cantidadDisponible=${entrada.cantidadDisponible}, " +
                              "entradasVendidas=${entrada.entradasVendidas}")
                    }
                } else {
                    Log.d("EventosCard", "Evento ${evento.titulo} - Sin entradas definidas")
                }
                
                // Verificar disponibilidad real
                val hayEntradasDisponibles = if (entradas.isEmpty()) {
                    // Si no hay entradas definidas, el evento no está disponible para compra
                    false
                } else {
                    // Verificamos si hay alguna entrada que cumpla con las condiciones:
                    // - Es ilimitada, O
                    // - Tiene cantidad disponible mayor que las vendidas, O
                    // - Tiene cantidad disponible NULL (lo que implica ilimitada) y no está marcada como ilimitada explícitamente
                    entradas.any { entrada ->
                        val esIlimitadaExplicita = entrada.esIlimitado
                        val esIlimitadaImplicita = entrada.cantidadDisponible == null && !entrada.esIlimitado
                        val tieneDisponibilidad = (entrada.cantidadDisponible ?: 0) > entrada.entradasVendidas
                        
                        val disponible = esIlimitadaExplicita || esIlimitadaImplicita || tieneDisponibilidad
                        Log.d("EventosCard", "  Entrada ${entrada.nombre}: " +
                              "esIlimitadaExplicita=$esIlimitadaExplicita, " + 
                              "esIlimitadaImplicita=$esIlimitadaImplicita, " +
                              "tieneDisponibilidad=$tieneDisponibilidad, " +
                              "-> disponible=$disponible")
                        disponible
                    }
                }
                
                Log.d("EventosCard", "  ¿Hay entradas disponibles?: $hayEntradasDisponibles")
                
                // Usar precio_minimo y precio_maximo del backend si están disponibles
                val precioMinimo = if (evento.precioMinimo != null) {
                    evento.precioMinimo
                } else if (hayEntradasDisponibles && entradas.isNotEmpty()) {
                    entradas.map { it.precio }.minOrNull() ?: 0.0
                } else {
                    0.0
                }
                
                val precioMaximo = if (evento.precioMaximo != null) {
                    evento.precioMaximo
                } else if (hayEntradasDisponibles && entradas.isNotEmpty()) {
                    entradas.map { it.precio }.maxOrNull() ?: 0.0
                } else {
                    0.0
                }
                
                Log.d("EventosCard", "Precios: min=${precioMinimo}, max=${precioMaximo}, hayEntradas=${hayEntradasDisponibles}")
                
                Text(
                    text = if (!hayEntradasDisponibles) {
                        stringResource(id = com.example.app.R.string.eventos_no_disponible)
                    } else if (precioMinimo == 0.0 && precioMaximo == 0.0) {
                        stringResource(id = com.example.app.R.string.eventos_gratuito)
                    } else if (precioMinimo == precioMaximo) {
                        "%.2f€".format(precioMinimo)
                    } else {
                        "%.2f€ - %.2f€".format(precioMinimo, precioMaximo)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (!hayEntradasDisponibles) 
                                textSecondaryColor 
                            else if (precioMinimo == 0.0 && precioMaximo == 0.0) 
                                successColor 
                            else 
                                primaryColor
                )
            }
        }
    }
}