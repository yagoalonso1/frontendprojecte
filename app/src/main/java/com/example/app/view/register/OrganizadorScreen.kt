package com.example.app.view.register

// Importaciones de Compose y Material3
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.viewmodel.RegisterViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import com.example.app.ui.components.LanguageAwareText
import com.example.app.util.LocaleHelper
import androidx.compose.ui.res.stringResource

@Composable
fun OrganizadorScreen(
    viewModel: RegisterViewModel
) {
    // Definir el tipo de usuario
    LaunchedEffect(Unit) {
        viewModel.role = "Organizador"
    }

    // Validación de campos
    val nombreOrganizacionValid = viewModel.nombreOrganizacion.isNotEmpty() && !viewModel.isNombreOrganizacionError
    val telefonoContactoValid = viewModel.telefonoContacto.length == 9 && !viewModel.isTelefonoContactoError
    
    // Verificar si se puede activar el botón
    val allFieldsFilled = viewModel.nombreOrganizacion.isNotEmpty() && viewModel.telefonoContacto.isNotEmpty()
    val allFieldsValid = nombreOrganizacionValid && telefonoContactoValid
    val registrationEnabled = allFieldsFilled && allFieldsValid
    
    // Pantalla principal
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo de la app
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.app_logo),
                    modifier = Modifier
                        .size(150.dp)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Título
                LanguageAwareText(
                    textId = R.string.organizer_title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE53935),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Resumen de datos personales
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        LanguageAwareText(
                            textId = R.string.organizer_summary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LanguageAwareText(
                                textId = R.string.register_name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        Text(
                                text = ": ${viewModel.name} ${viewModel.apellido1} ${viewModel.apellido2}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LanguageAwareText(
                                textId = R.string.register_email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        Text(
                                text = ": ${viewModel.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        }
                    }
                }
                
                // Sección de información de la organización
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Campo para el nombre de la organización
                    OutlinedTextField(
                        value = viewModel.nombreOrganizacion,
                        onValueChange = { 
                            viewModel.nombreOrganizacion = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("nombreOrganizacion", it)
                            } else {
                                viewModel.isNombreOrganizacionError = false
                            }
                        },
                        label = { LanguageAwareText(textId = R.string.organizer_org_name) },
                        isError = viewModel.isNombreOrganizacionError,
                        supportingText = {
                            if (viewModel.isNombreOrganizacionError) {
                                LanguageAwareText(
                                    textId = R.string.organizer_error_org_name,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFE53935),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo para el teléfono de contacto
                    OutlinedTextField(
                        value = viewModel.telefonoContacto,
                        onValueChange = { 
                            // Solo permitir dígitos y limitar a 9
                            if (it.all { c -> c.isDigit() } && it.length <= 9) {
                                viewModel.telefonoContacto = it
                                if (it.isNotEmpty()) {
                                    viewModel.validateField("telefonoContacto", it)
                                } else {
                                    viewModel.isTelefonoContactoError = false
                                }
                            }
                        },
                        label = { LanguageAwareText(textId = R.string.organizer_contact_phone) },
                        isError = viewModel.isTelefonoContactoError,
                        supportingText = {
                            if (viewModel.isTelefonoContactoError) {
                                if (viewModel.telefonoContacto.isEmpty()) {
                                    LanguageAwareText(
                                        textId = R.string.organizer_error_phone_required,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    LanguageAwareText(
                                        textId = R.string.organizer_error_phone_digits,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else if (viewModel.telefonoContacto.isNotEmpty() && viewModel.telefonoContacto.length < 9) {
                                val remaining = 9 - viewModel.telefonoContacto.length
                                LanguageAwareText(
                                    textId = R.string.participant_phone_digits,
                                    formatArgs = arrayOf(remaining)
                                )
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botón de completar registro
                    Button(
                        onClick = { 
                            viewModel.onRegisterClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 16.dp),
                        enabled = registrationEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        LanguageAwareText(
                            textId = R.string.organizer_complete,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
            
            // Mensaje de error
            val errorMessage by viewModel.errorMessage.collectAsState()
            if (viewModel.isError) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { LanguageAwareText(textId = R.string.register_error_title) },
                    text = { Text(errorMessage ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            LanguageAwareText(textId = R.string.ok_button)
                        }
                    }
                )
            }
            
            // Mensaje de éxito
            val isRegisterSuccessful by viewModel.isRegisterSuccessful.collectAsState()
            if (isRegisterSuccessful) {
                AlertDialog(
                    onDismissRequest = { /* No hacer nada, forzar al usuario a hacer clic en el botón */ },
                    title = { 
                        LanguageAwareText(
                            textId = R.string.organizer_success,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4CAF50)
                        )
                    },
                    text = { 
                        LanguageAwareText(
                            textId = R.string.organizer_success_message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { /* No hacer nada */ }) {
                            LanguageAwareText(
                                textId = R.string.participant_done,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4CAF50)
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
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
    }
}

fun validateOrganizador(nombreOrganizacion: String, telefonoContacto: String): Boolean {
    return nombreOrganizacion.isNotEmpty() && telefonoContacto.length >= 9
}