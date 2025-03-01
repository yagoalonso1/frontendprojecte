package com.example.app.view.register

// Importaciones de Compose y Material3
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.viewmodel.RegisterViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ParticipanteScreen(
    viewModel: RegisterViewModel
) {
    // Eliminar esta variable ya que no queremos mostrar la contraseña
    // var passwordVisible by remember { mutableStateOf(false) }
    
    // Añadir variable para mostrar los requisitos de contraseña
    var showPasswordRequirements by remember { mutableStateOf(false) }
    
    // Verificamos los campos solo si el usuario ha interactuado con ellos
    val dniValid = viewModel.dni.isEmpty() || !viewModel.isDniError
    val telefonoValid = viewModel.telefono.isEmpty() || !viewModel.isTelefonoError
    
    // Verificamos si todos los campos requeridos están completos y válidos
    val allFieldsFilled = viewModel.dni.isNotEmpty() && viewModel.telefono.isNotEmpty()
    val allFieldsValid = !viewModel.isDniError && !viewModel.isTelefonoError
    val buttonEnabled = allFieldsFilled && allFieldsValid

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White // Fondo blanco para una interfaz minimalista
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            item {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "EventFlix Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Título
            item {
                Text(
                    text = "Datos de Participante",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935) // Color rojo del logo
                    ),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Campos de formulario
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    // Resumen de datos principales
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Información básica",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "Nombre: ${viewModel.name} ${viewModel.apellido1} ${viewModel.apellido2}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Email: ${viewModel.email}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                    
                    // Campos específicos de Participante
                    OutlinedTextField(
                        value = viewModel.dni,
                        onValueChange = { 
                            viewModel.dni = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("dni", it)
                            } else {
                                viewModel.isDniError = false
                            }
                        },
                        label = { Text("DNI") },
                        isError = !dniValid,
                        supportingText = {
                            if (!dniValid) {
                                Text("DNI inválido (formato: 12345678A)")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFE53935),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = viewModel.telefono,
                        onValueChange = { 
                            viewModel.telefono = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("telefono", it)
                            } else {
                                viewModel.isTelefonoError = false
                            }
                        },
                        label = { Text("Teléfono") },
                        isError = !telefonoValid,
                        supportingText = {
                            if (!telefonoValid) {
                                Text("Teléfono inválido (debe tener 9 dígitos)")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFE53935),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }
            }

            // Botón de registro
            item {
                Button(
                    onClick = {
                        // Asegurarse de que la validación sea correcta
                        if (allFieldsFilled && allFieldsValid) {
                            viewModel.role = "Participante" // Asegurarse de que el rol esté establecido
                            viewModel.onRegisterClick()
                        } else {
                            viewModel.setError("Por favor, completa todos los campos correctamente")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(vertical = 4.dp),
                    enabled = buttonEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "Completar Registro",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                
                // Mensaje de ayuda si el botón está deshabilitado
                if (!buttonEnabled) {
                    Text(
                        text = "Completa todos los campos correctamente para continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
    
    // Diálogo de error
    if (viewModel.isError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { 
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                ) 
            },
            text = { 
                Text(
                    text = viewModel.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        text = "Aceptar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    )
                }
            },
            containerColor = Color.White,
            iconContentColor = Color(0xFFE53935)
        )
    }
    
    // Diálogo de éxito
    val isRegisterSuccessful by viewModel.isRegisterSuccessful.collectAsState()
    if (isRegisterSuccessful) {
        AlertDialog(
            onDismissRequest = { /* No hacer nada, la navegación se maneja en AppNavHost */ },
            title = { 
                Text(
                    text = "¡Registro Exitoso!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                ) 
            },
            text = { 
                Text(
                    text = "Tu cuenta ha sido creada correctamente. Serás redirigido a la pantalla de inicio de sesión.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                // Quitamos la acción del botón para evitar interferencias con la navegación automática
                TextButton(onClick = { /* No hacer nada, la navegación se maneja en AppNavHost */ }) {
                    Text(
                        text = "Hecho",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    )
                }
            },
            containerColor = Color.White,
            iconContentColor = Color(0xFF4CAF50)
        )
    }
    
    // Indicador de carga
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFE53935),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}