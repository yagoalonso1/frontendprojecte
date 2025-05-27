package com.example.app.view.organizador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.model.Evento
import com.example.app.model.Organizador
import com.example.app.model.getOrganizadorAvatarUrl
import com.example.app.routes.Routes
import com.example.app.view.EventoCard
import com.example.app.viewmodel.OrganizadorDetailViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.app.R
import com.example.app.ui.components.LanguageAwareText
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizadorDetailScreen(
    navController: NavController,
    organizador: Organizador
) {
    val viewModel: OrganizadorDetailViewModel = viewModel()
    
    // Cargar datos cuando se inicia la pantalla
    LaunchedEffect(organizador.id) { 
        Log.d("OrganizadorDetailScreen", "Cargando datos para organizador ID: ${organizador.id}")
        viewModel.loadEventos(organizador.id)
    }
    
    // Obtener datos del ViewModel
    val eventos by remember { derivedStateOf { viewModel.eventos } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val isError by remember { derivedStateOf { viewModel.isError } }
    val organizadorData by remember { derivedStateOf { viewModel.getOrganizador() ?: organizador } }

    // Colores y estilos
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val cardBackgroundColor = Color(0xFFF9F9F9)
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.DarkGray
    val successColor = Color(0xFF4CAF50)
    
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Calcular la URL del avatar usando solo el campo avatar o user?.avatar
    val avatar = organizadorData.avatar ?: organizadorData.user?.avatar

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        LanguageAwareText(
                            textId = R.string.organizer_detail_title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button),
                                tint = primaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = primaryColor,
                        navigationIconContentColor = primaryColor
                    ),
                    actions = {
                        // Botón de Favorito (solo para participantes)
                        if (viewModel.puedeMarcarFavorito) {
                            val isFavorito = viewModel.isFavorito
                            Log.d("OrganizadorDetailScreen", "Renderizando corazón: isFavorito = $isFavorito (🔴=${isFavorito} ⚪️=${!isFavorito})")
                            val toggleFavoritoLoading = viewModel.toggleFavoritoLoading.collectAsState()
                            
                            IconButton(
                                onClick = { 
                                    Log.d("OrganizadorDetailScreen", "💥 BOTÓN FAVORITO PULSADO - Estado antes: $isFavorito")
                                    viewModel.toggleFavorito(organizador.id) 
                                },
                                enabled = !toggleFavoritoLoading.value,
                                modifier = Modifier.size(56.dp)
                            ) {
                                if (toggleFavoritoLoading.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = primaryColor,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Log.d("OrganizadorDetailScreen", "🎨 Renderizando icono, estado isFavorito=$isFavorito")
                                    Icon(
                                        imageVector = if (isFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorito) 
                                            stringResource(R.string.evento_detalle_quitar_favoritos) 
                                            else 
                                            stringResource(R.string.evento_detalle_anadir_favoritos),
                                        tint = if (isFavorito) primaryColor else Color.Gray,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar y nombre
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .shadow(4.dp, CircleShape)
                                .background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                if (!avatar.isNullOrEmpty()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(avatar)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = stringResource(R.string.organizer_avatar_desc),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            CircularProgressIndicator(
                                                color = primaryColor,
                                                modifier = Modifier.padding(40.dp)
                                            )
                                        },
                                        error = {
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = CircleShape,
                                                color = primaryColor.copy(alpha = 0.15f),
                                                border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
                                            ) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = stringResource(R.string.default_avatar),
                                                    modifier = Modifier
                                                        .padding(24.dp)
                                                        .size(64.dp),
                                                    tint = primaryColor
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = primaryColor.copy(alpha = 0.15f),
                                        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = stringResource(R.string.default_avatar),
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .size(64.dp),
                                            tint = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Nombre del organizador
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = organizadorData.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )
                    }
                }

                // Switch para mostrar más información
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LanguageAwareText(
                                textId = R.string.organizer_detail_show_more,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textPrimaryColor
                            )
                            Switch(
                                checked = expanded,
                                onCheckedChange = { expanded = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryColor,
                                    checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }
                }

                // Sección de información adicional
                if (expanded) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Sección información del organizador
                                SectionHeader(
                                    title = stringResource(R.string.organizer_detail_info),
                                    icon = Icons.Default.Business,
                                    color = primaryColor
                                )
                                
                                HorizontalDivider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                // Teléfono (siempre debe estar disponible)
                                InfoRow(
                                    label = stringResource(R.string.organizer_detail_phone) + ":",
                                    value = organizadorData.telefonoContacto,
                                    icon = Icons.Default.Phone
                                )
                                
                                // Dirección fiscal
                                organizadorData.direccionFiscal?.let { direccion ->
                                    InfoRow(
                                        label = stringResource(R.string.organizer_detail_address) + ":",
                                        value = direccion,
                                        icon = Icons.Default.LocationOn
                                    )
                                }
                                
                                // CIF
                                organizadorData.cif?.let { cif ->
                                    InfoRow(
                                        label = stringResource(R.string.organizer_detail_cif) + ":",
                                        value = cif,
                                        icon = Icons.Default.Info
                                    )
                                }
                                
                                // Información de contacto (usuario)
                                organizadorData.user?.let { user ->
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Encabezado de contacto
                                    SectionHeader(
                                        title = stringResource(R.string.organizer_detail_contact_info),
                                        icon = Icons.Default.Person,
                                        color = primaryColor
                                    )
                                    
                                    HorizontalDivider(
                                        color = Color.LightGray,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Nombre
                                    InfoRow(
                                        label = stringResource(R.string.register_name) + ":",
                                        value = organizadorData.nombreUsuario ?: user.nombre,
                                        icon = Icons.Default.Person
                                    )
                                    
                                    // Email
                                    InfoRow(
                                        label = stringResource(R.string.register_email) + ":",
                                        value = user.email,
                                        icon = Icons.Default.Email
                                    )
                                }
                            }
                        }
                    }
                }

                // Sección de eventos
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.organizer_detail_events) + ":",
                            icon = Icons.Default.Event,
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                    }
                }

                // Mostrar eventos según el estado
                when {
                    isLoading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                    isError -> item {
                        ErrorMessage(
                            message = stringResource(R.string.organizer_detail_error_events),
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                    eventos.isEmpty() -> item {
                        EmptyStateMessage(
                            message = stringResource(R.string.organizer_detail_no_events),
                            modifier = Modifier.padding(24.dp, bottom = 56.dp)
                        )
                    }
                    else -> items(eventos, key = { it.getEventoId() }) { evento ->
                        EventoCard(
                            evento = evento,
                            onClick = {
                                navController.navigate(
                                    Routes.EventoDetalle.createRoute(evento.getEventoId().toString())
                                )
                            },
                            primaryColor = primaryColor,
                            textPrimaryColor = textPrimaryColor,
                            textSecondaryColor = textSecondaryColor,
                            successColor = successColor
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// Función composable para mostrar una fila de información
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    maxLines: Int = 3
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                maxLines = maxLines,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// Cabecera de sección con icono
@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// Mensaje de error
@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Mensaje de estado vacío
@Composable
private fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
} 