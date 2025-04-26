package com.example.app.view.organizador

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import com.example.app.model.Organizador
import com.example.app.routes.Routes
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.derivedStateOf
import com.example.app.view.EventoCard
import com.example.app.viewmodel.OrganizadorDetailViewModel

@Composable
fun OrganizadorDetailScreen(
    navController: NavController,
    organizador: Organizador
) {
    val viewModel: OrganizadorDetailViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }
    val eventos by remember { derivedStateOf { viewModel.eventos } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val isError by remember { derivedStateOf { viewModel.isError } }
    LaunchedEffect(organizador.id) {
        viewModel.loadEventos(organizador.id)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header: logo y nombre de la organización
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(120.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = organizador.nombre,
                style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Toggle detalles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (expanded) "Ocultar detalles" else "Ver detalles",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: ${organizador.id}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Teléfono: ${organizador.telefonoContacto}",
                    style = MaterialTheme.typography.bodyMedium
                )
                organizador.direccionFiscal?.let { direccion ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dirección: $direccion",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                organizador.cif?.let { cif ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CIF: $cif",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                organizador.user?.let { user ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Propietario: ${user.nombre} ${user.apellido1}${user.apellido2?.let { " $it" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Email: ${user.email}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        // Sección Eventos
        item {
            Text(
                text = "Eventos",
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
        }
        // Lista de eventos
        when {
            isLoading -> item { Text(text = "Cargando...", style = MaterialTheme.typography.bodyMedium) }
            isError -> item { Text(text = "Error al cargar eventos", color = Color.Red) }
            else -> items(
                items = eventos,
                key = { it.getEventoId() }
            ) { evento ->
                EventoCard(
                    evento = evento,
                    onClick = {
                        navController.navigate(Routes.EventoDetalle.createRoute(evento.getEventoId().toString()))
                    },
                    primaryColor = MaterialTheme.colorScheme.primary,
                    textPrimaryColor = MaterialTheme.colorScheme.onBackground,
                    textSecondaryColor = Color.Gray,
                    successColor = Color(0xFF4CAF50)
                )
            }
        }
    }
} 