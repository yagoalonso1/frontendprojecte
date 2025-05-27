package com.example.app.view.favoritos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app.R
import com.example.app.model.Evento
import com.example.app.model.getOrganizadorAvatarUrl
import com.example.app.routes.BottomNavigationBar
import com.example.app.routes.Routes
import com.example.app.util.formatDate
import com.example.app.util.getImageUrl
import com.example.app.viewmodel.favoritos.FavoritosViewModel
import kotlinx.coroutines.launch
import com.example.app.view.organizador.OrganizadorCard
import com.example.app.view.EventoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    navController: NavHostController,
    viewModel: FavoritosViewModel = viewModel()
) {
    val favoritos = viewModel.favoritos
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val errorMessage = viewModel.errorMessage
    
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color.White
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.DarkGray
    val successColor = Color(0xFF4CAF50)
    
    // Estado para controlar la pestaña seleccionada
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Efecto para recargar los favoritos cuando se muestra la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadFavoritos()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.favoritos_titulo),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor
                    ) 
                },
                actions = {
                    // Botón para recargar favoritos
                    IconButton(
                        onClick = { viewModel.loadFavoritos() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = primaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.favoritos_actualizar),
                                tint = primaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = primaryColor
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = "Participante"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            // Tabs para seleccionar entre eventos y organizadores
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = backgroundColor,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(3.dp)
                                .background(color = primaryColor)
                    )
                    }
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = stringResource(id = R.string.favoritos_eventos),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) primaryColor else textSecondaryColor
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 0) primaryColor else textSecondaryColor
                        )
                    }
                )
                
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = stringResource(id = R.string.favoritos_organizadores),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) primaryColor else textSecondaryColor
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 1) primaryColor else textSecondaryColor
                        )
                    }
                )
            }
            
            // Contenido según la pestaña seleccionada
            when (selectedTabIndex) {
                0 -> EventosFavoritosContent(
                    favoritos = favoritos,
                    isLoading = isLoading,
                    isError = isError,
                    errorMessage = errorMessage,
                    navController = navController,
                    primaryColor = primaryColor,
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor,
                    successColor = successColor
                )
                1 -> OrganizadoresFavoritosContent(
                    navController = navController,
                    primaryColor = primaryColor,
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun EventosFavoritosContent(
    favoritos: List<Evento>,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String?,
    navController: NavHostController,
    primaryColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    successColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            isError -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = primaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = errorMessage ?: stringResource(id = R.string.favoritos_error_login),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textPrimaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            favoritos.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.LightGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(id = R.string.favoritos_no_eventos),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { navController.navigate(Routes.Eventos.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(stringResource(id = R.string.favoritos_explorar_eventos))
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoritos) { evento ->
                        EventoCard(
                            evento = evento,
                            onClick = { 
                                navController.navigate(Routes.EventoDetalle.createRoute(evento.getEventoId().toString()))
                            },
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

@Composable
private fun OrganizadoresFavoritosContent(
    navController: NavHostController,
    primaryColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    viewModel: FavoritosViewModel = viewModel()
) {
    val organizadores = viewModel.organizadoresFavoritos
    val isLoading = viewModel.isLoadingOrganizadores

    // Efecto para recargar cuando se muestra el contenido
    LaunchedEffect(Unit) {
        viewModel.loadOrganizadoresFavoritos()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            organizadores.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.LightGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(id = R.string.favoritos_no_organizadores),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { navController.navigate(Routes.Eventos.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(stringResource(id = R.string.favoritos_explorar_organizadores))
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    // Lista de organizadores
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(organizadores) { organizador ->
                            OrganizadorCard(
                                organizador = organizador,
                                onClick = {
                                    navController.navigate(Routes.OrganizadorDetalle.createRoute(organizador.id))
                                },
                                onFavoriteClick = {
                                    viewModel.toggleOrganizadorFavorito(organizador)
                                },
                                primaryColor = primaryColor,
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
