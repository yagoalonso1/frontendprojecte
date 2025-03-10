package com.example.app.view.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.viewmodel.RegisterViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var showPasswordRequirements by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var repeatPasswordVisible by remember { mutableStateOf(false) }
    
    // Verificamos los campos solo si el usuario ha interactuado con ellos
    val nameValid = viewModel.name.isEmpty() || !viewModel.isNameError
    val apellido1Valid = viewModel.apellido1.isEmpty() || !viewModel.isApellido1Error
    val apellido2Valid = viewModel.apellido2.isEmpty() || !viewModel.isApellido2Error
    val emailValid = viewModel.email.isEmpty() || !viewModel.isEmailError
    val passwordValid = viewModel.password.isEmpty() || !viewModel.isPasswordError
    val repeatPasswordValid = viewModel.repeatPassword.isEmpty() || !viewModel.isRepeatPasswordError
    
    // Verificamos si todos los campos requeridos están completos y válidos
    val allFieldsFilled = viewModel.name.isNotEmpty() && 
                         viewModel.apellido1.isNotEmpty() && 
                         viewModel.email.isNotEmpty() && 
                         viewModel.password.isNotEmpty() && 
                         viewModel.repeatPassword.isNotEmpty()
    
    val allFieldsValid = !viewModel.isNameError && 
                         !viewModel.isApellido1Error && 
                         !viewModel.isEmailError && 
                         !viewModel.isPasswordError && 
                         !viewModel.isRepeatPasswordError &&
                         viewModel.doPasswordsMatch()
    
    val buttonsEnabled = allFieldsFilled && allFieldsValid

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
                        .size(180.dp)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Título
            item {
                Text(
                    text = "Registro de Usuario",
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
                    // Campos comunes para ambos roles
                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = { 
                            viewModel.name = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("name", it)
                            } else {
                                viewModel.isNameError = false
                            }
                        },
                        label = { Text("Nombre") },
                        isError = !nameValid,
                        supportingText = {
                            if (!nameValid) {
                                Text("Nombre inválido")
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
                        value = viewModel.apellido1,
                        onValueChange = { 
                            viewModel.apellido1 = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("apellido1", it)
                            } else {
                                viewModel.isApellido1Error = false
                            }
                        },
                        label = { Text("Primer Apellido") },
                        isError = !apellido1Valid,
                        supportingText = {
                            if (!apellido1Valid) {
                                Text("Apellido inválido")
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
                        value = viewModel.apellido2,
                        onValueChange = { 
                            viewModel.apellido2 = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("apellido2", it)
                            } else {
                                viewModel.isApellido2Error = false
                            }
                        },
                        label = { Text("Segundo Apellido") },
                        isError = !apellido2Valid,
                        supportingText = {
                            if (!apellido2Valid) {
                                Text("Apellido inválido")
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
                        value = viewModel.email,
                        onValueChange = { 
                            viewModel.email = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("email", it)
                            } else {
                                viewModel.isEmailError = false
                            }
                        },
                        label = { Text("Email") },
                        isError = !emailValid,
                        supportingText = {
                            if (!emailValid) {
                                Text("Email inválido")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
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
                        value = viewModel.password,
                        onValueChange = { 
                            viewModel.password = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("password", it)
                            } else {
                                viewModel.isPasswordError = false
                            }
                        },
                        label = { Text("Contraseña") },
                        isError = !passwordValid,
                        supportingText = {
                            if (!passwordValid) {
                                Text("Contraseña inválida")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            Row {
                                // Botón de información para requisitos de contraseña
                                IconButton(onClick = { showPasswordRequirements = !showPasswordRequirements }) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Requisitos de contraseña",
                                        tint = Color(0xFFE53935)
                                    )
                                }
                                // Botón para mostrar/ocultar contraseña
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = Color(0xFFE53935)
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
                    
                    // Mostrar los requisitos de contraseña si se hace clic en el botón de información
                    if (showPasswordRequirements) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                    text = "Requisitos de contraseña:",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF333333),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Text(
                                    text = "• Al menos 8 caracteres",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "• Al menos una letra mayúscula",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "• Al menos una letra minúscula",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "• Al menos un número",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "• Al menos un carácter especial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = viewModel.repeatPassword,
                        onValueChange = { 
                            viewModel.repeatPassword = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("repeatPassword", it)
                            } else {
                                viewModel.isRepeatPasswordError = false
                            }
                        },
                        label = { Text("Confirmar Contraseña") },
                        isError = !repeatPasswordValid,
                        supportingText = {
                            if (!repeatPasswordValid) {
                                Text("Las contraseñas no coinciden")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                                Icon(
                                    imageVector = if (repeatPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (repeatPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = Color(0xFFE53935)
                                )
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

            // Selección de rol
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecciona tu rol",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.role = "Organizador"
                                navController.navigate("register/organizador")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .height(48.dp),
                            enabled = buttonsEnabled,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Text(
                                text = "Organizador",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.role = "Participante"
                                navController.navigate("register/participante")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .height(48.dp),
                            enabled = buttonsEnabled,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Text(
                                text = "Participante",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Mensaje de ayuda si los botones están deshabilitados
                    if (!buttonsEnabled) {
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
            
            // Botón para volver al login
            item {
                TextButton(
                    onClick = { 
                        navController.navigate("login") 
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = Color(0xFFE53935),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
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
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE53935)
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
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFE53935)
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