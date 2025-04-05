package com.example.app.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.routes.BottomNavigationBar
import com.example.app.util.SessionManager
import com.example.app.viewmodel.CrearEventoViewModel
import java.util.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    navController: NavController,
    viewModel: CrearEventoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val userRole = SessionManager.getUserRole() ?: "participante"

    // Lista de categorías disponibles
    val categorias = listOf(
        "Concierto",
        "Festival",
        "Teatro",
        "Deportes",
        "Conferencia",
        "Exposición",
        "Taller",
        "Otro"
    )

    // Estado para el dropdown de categorías
    var expandedCategoria by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePicker.launch("image/*")
        } else {
            Toast.makeText(context, "Se necesita permiso para acceder a la galería", Toast.LENGTH_LONG).show()
        }
    }

    fun checkAndRequestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {
                        imagePicker.launch("image/*")
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            else -> {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                        imagePicker.launch("image/*")
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "CREAR EVENTO",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFE53935),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, userRole = userRole)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Campos del formulario con estilo unificado
            OutlinedTextField(
                value = viewModel.nombreEvento,
                onValueChange = { viewModel.nombreEvento = it },
                label = { Text("Nombre del evento") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935)
                )
            )

            OutlinedTextField(
                value = viewModel.descripcion,
                onValueChange = { viewModel.descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935)
                )
            )

            // Selector de fecha con validación
            OutlinedTextField(
                value = viewModel.fechaEvento,
                onValueChange = {},
                label = { Text("Fecha del evento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(year, month, day, 0, 0, 0)
                                selectedCalendar.set(Calendar.MILLISECOND, 0)
                                
                                val today = Calendar.getInstance()
                                today.set(Calendar.HOUR_OF_DAY, 0)
                                today.set(Calendar.MINUTE, 0)
                                today.set(Calendar.SECOND, 0)
                                today.set(Calendar.MILLISECOND, 0)
                                
                                if (selectedCalendar.before(today)) {
                                    Toast.makeText(context, "No puedes seleccionar una fecha pasada", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.fechaEvento = String.format("%04d-%02d-%02d", year, month + 1, day)
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, "Seleccionar fecha", tint = Color(0xFFE53935))
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935),
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledTextColor = Color.Black
                )
            )

            // Selector de hora con validación
            OutlinedTextField(
                value = viewModel.hora,
                onValueChange = {},
                label = { Text("Hora del evento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                                selectedCalendar.set(Calendar.MINUTE, minute)
                                
                                val now = Calendar.getInstance()
                                
                                // Si la fecha seleccionada es hoy, verificar que la hora no sea pasada
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val today = dateFormat.format(now.time)
                                
                                if (viewModel.fechaEvento == today && 
                                    (selectedCalendar.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY) || 
                                    (selectedCalendar.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) && 
                                     selectedCalendar.get(Calendar.MINUTE) < now.get(Calendar.MINUTE)))) {
                                    Toast.makeText(context, "No puedes seleccionar una hora pasada para hoy", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.hora = String.format("%02d:%02d", hour, minute)
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.Schedule, "Seleccionar hora", tint = Color(0xFFE53935))
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935),
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledTextColor = Color.Black
                )
            )

            OutlinedTextField(
                value = viewModel.ubicacion,
                onValueChange = { viewModel.ubicacion = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935),
                    focusedLabelColor = Color(0xFFE53935)
                )
            )

            // Selector de categoría con dropdown y estado de carga
            ExposedDropdownMenuBox(
                expanded = expandedCategoria,
                onExpandedChange = { expandedCategoria = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        if (viewModel.isLoadingCategorias) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFE53935)
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE53935),
                        focusedLabelColor = Color(0xFFE53935)
                    ),
                    enabled = !viewModel.isLoadingCategorias
                )
                if (!viewModel.isLoadingCategorias) {
                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        viewModel.categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria) },
                                onClick = {
                                    viewModel.categoria = categoria
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
                }
            }

            if (viewModel.errorCategorias != null) {
                Text(
                    text = viewModel.errorCategorias!!,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Selector de imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { checkAndRequestPermission() },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.imagen != null) {
                    AsyncImage(
                        model = viewModel.imagen,
                        contentDescription = "Imagen del evento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Añadir imagen",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFE53935)
                        )
                        Text(
                            text = "Añadir imagen",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }

            if (viewModel.error != null) {
                Text(
                    text = viewModel.error!!,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Selector online / presencial
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evento online",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = viewModel.esOnline,
                    onCheckedChange = { viewModel.esOnline = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFE53935),
                        checkedTrackColor = Color(0xFFE53935).copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de tipos de entrada (solo visible si el evento no es online)
            if (!viewModel.esOnline) {
                Text(
                    text = "TIPOS DE ENTRADA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Añade al menos un tipo de entrada para tu evento",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lista de tipos de entrada
                viewModel.tiposEntrada.forEachIndexed { index, tipoEntrada ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Cabecera con título y botón eliminar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tipo de entrada ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                                if (viewModel.tiposEntrada.size > 1) {
                                    IconButton(onClick = { viewModel.removeTipoEntrada(index) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar tipo de entrada",
                                            tint = Color(0xFFE53935)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Nombre del tipo de entrada
                            OutlinedTextField(
                                value = tipoEntrada.nombre,
                                onValueChange = { 
                                    val updatedTipo = tipoEntrada.copy(nombre = it)
                                    viewModel.updateTipoEntrada(index, updatedTipo)
                                },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE53935),
                                    focusedLabelColor = Color(0xFFE53935)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Precio
                            OutlinedTextField(
                                value = tipoEntrada.precio,
                                onValueChange = { 
                                    val updatedTipo = tipoEntrada.copy(precio = it)
                                    viewModel.updateTipoEntrada(index, updatedTipo)
                                },
                                label = { Text("Precio (€)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE53935),
                                    focusedLabelColor = Color(0xFFE53935)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Descripción
                            OutlinedTextField(
                                value = tipoEntrada.descripcion,
                                onValueChange = { 
                                    val updatedTipo = tipoEntrada.copy(descripcion = it)
                                    viewModel.updateTipoEntrada(index, updatedTipo)
                                },
                                label = { Text("Descripción (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE53935),
                                    focusedLabelColor = Color(0xFFE53935)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Switch para entradas ilimitadas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Entradas ilimitadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Switch(
                                    checked = tipoEntrada.esIlimitado,
                                    onCheckedChange = { 
                                        val updatedTipo = tipoEntrada.copy(esIlimitado = it)
                                        viewModel.updateTipoEntrada(index, updatedTipo)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFE53935),
                                        checkedTrackColor = Color(0xFFE53935).copy(alpha = 0.5f)
                                    )
                                )
                            }
                            
                            // Campo de cantidad disponible (solo visible si no es ilimitado)
                            if (!tipoEntrada.esIlimitado) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = tipoEntrada.cantidadDisponible,
                                    onValueChange = { 
                                        val updatedTipo = tipoEntrada.copy(cantidadDisponible = it)
                                        viewModel.updateTipoEntrada(index, updatedTipo)
                                    },
                                    label = { Text("Cantidad disponible") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935)
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Botón para añadir otro tipo de entrada
                Button(
                    onClick = { viewModel.addTipoEntrada() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFE53935)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir tipo de entrada",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir tipo de entrada")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { viewModel.crearEvento(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Crear Evento",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            Toast.makeText(context, "Evento creado exitosamente", Toast.LENGTH_SHORT).show()
            navController.navigate("eventos")
        }
    }
}