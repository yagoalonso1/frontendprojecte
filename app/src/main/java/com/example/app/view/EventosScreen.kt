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
import com.example.app.viewmodel.EventoViewModel
import kotlinx.coroutines.launch
import com.example.app.routes.BottomNavigationBar
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    onEventoClick: (Evento) -> Unit,
    navController: NavController = rememberNavController()
) {
    // Obtener el rol del usuario desde el savedStateHandle
    val userRole = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("user_role") ?: ""
    
    val viewModel: EventoViewModel = viewModel()
    val eventos = viewModel.eventos
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    
    // Estado para la búsqueda
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Estado para controlar la visibilidad de la barra de búsqueda
    val lazyListState = rememberLazyListState()
    val isSearchBarVisible = remember { 
        derivedStateOf { 
            // Mostrar la barra de búsqueda cuando estamos en la parte superior de la lista
            lazyListState.firstVisibleItemIndex == 0 && 
            (lazyListState.firstVisibleItemScrollOffset == 0 || lazyListState.firstVisibleItemScrollOffset < 50)
        } 
    }
    
    // Filtrar eventos según la búsqueda
    val filteredEventos = remember(searchQuery, eventos) {
        if (searchQuery.isBlank()) {
            eventos
        } else {
            eventos.filter { evento ->
                evento.titulo.contains(searchQuery, ignoreCase = true) ||
                evento.ubicacion.contains(searchQuery, ignoreCase = true) ||
                evento.fechaEvento.contains(searchQuery, ignoreCase = true) ||
                evento.categoria.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Colores consistentes con LoginScreen
    val primaryColor = Color(0xFFE53935)  // Rojo del logo
    val backgroundColor = Color.White
    val cardColor = Color.White
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.DarkGray  // Más oscuro para mejor contraste
    val successColor = Color(0xFF4CAF50)  // Verde para elementos gratuitos
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "EVENTOS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor,
                        modifier = Modifier.padding(start = 8.dp)
                    ) 
                },
                actions = {
                    // Botón de perfil de usuario
                    IconButton(
                        onClick = { /* Implementa el clic del botón de perfil */ },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, primaryColor, CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil de usuario",
                            tint = primaryColor,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,  // Fondo blanco para la barra superior
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
                        text = viewModel.errorMessage ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Barra de búsqueda animada
                    AnimatedVisibility(
                        visible = isSearchBarVisible.value,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Campo de búsqueda
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                                placeholder = { Text("Buscar eventos...") },
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = { focusManager.clearFocus() }
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Botón de búsqueda
                            Button(
                                onClick = { focusManager.clearFocus() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(4.dp, CircleShape),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    // Lista de eventos
                    LazyColumn(
                        state = lazyListState,
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
                    .data("https://eventosapp.jmrp.es/storage/${evento.imagen}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen del evento",
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
                // Categoría
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = evento.categoria,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Fecha",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = formatDate(evento.fechaEvento, true),  // Ahora incluye el año
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Hora",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = if (evento.hora.length >= 5) evento.hora.substring(0, 5) else evento.hora,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
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
                Text(
                    text = if (evento.precio > 0) "${evento.precio}€" else "Gratuito",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (evento.precio > 0) primaryColor else successColor
                )
            }
        }
    }
}