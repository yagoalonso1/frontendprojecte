package com.example.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.util.CategoryTranslator
import com.example.app.viewmodel.EventosCategoriaViewModel
import com.example.app.model.Evento
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app.util.formatDate
import com.example.app.util.getImageUrl
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosCategoriaScreen(
    navController: NavController,
    categoria: String,
    viewModel: EventosCategoriaViewModel = viewModel()
) {
    // Colores consistentes con el resto de la aplicación
    val primaryColor = Color(0xFFE53935)
    val textPrimaryColor = Color.Black
    val textSecondaryColor = Color.Gray
    val successColor = Color(0xFF4CAF50)
    
    // Obtener la categoría traducida
    val categoriaTraducida = CategoryTranslator.translate(categoria)
    
    // Cargar eventos de esta categoría
    LaunchedEffect(categoria) {
        viewModel.loadEventosByCategoria(categoria)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = categoriaTraducida.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = primaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(id = com.example.app.R.string.back_button),
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (viewModel.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (viewModel.errorMessage != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${viewModel.errorMessage}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                } else if (viewModel.eventos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = com.example.app.R.string.eventos_no_hay_categoria),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondaryColor
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viewModel.eventos) { evento ->
                            EventoCard(
                                evento = evento,
                                onClick = {
                                    navController.navigate("evento_detail/${evento.idEvento}")
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
