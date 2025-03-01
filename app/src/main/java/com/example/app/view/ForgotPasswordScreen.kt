package com.example.app.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "EventFlix Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Título
                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                
                // Indicador de pasos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StepIndicator(step = 1, currentStep = currentStep)
                    Spacer(modifier = Modifier.width(8.dp))
                    StepIndicator(step = 2, currentStep = currentStep)
                    Spacer(modifier = Modifier.width(8.dp))
                    StepIndicator(step = 3, currentStep = currentStep)
                }
                
                // Contenido según el paso actual
                when (currentStep) {
                    1 -> {
                        // Paso 1: Ingresar email
                        Text(
                            text = "Ingresa tu correo electrónico para buscar tu cuenta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
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
                        
                        Button(
                            onClick = { 
                                if (email.isNotEmpty()) {
                                    // Simulamos verificación del email
                                    viewModel.verifyEmail(email)
                                    currentStep = 2
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935)
                            )
                        ) {
                            Text(
                                text = "Buscar Cuenta",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    2 -> {
                        // Paso 2: Responder pregunta de seguridad
                        Text(
                            text = "Responde tu pregunta de seguridad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Pregunta de seguridad:",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF333333),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Text(
                                    text = "¿Cuál es el nombre de tu primera mascota?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = securityAnswer,
                            onValueChange = { securityAnswer = it },
                            label = { Text("Tu respuesta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
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
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.dp,
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE53935))
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = "Atrás",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    if (securityAnswer.isNotEmpty()) {
                                        // Verificamos la respuesta (simulado)
                                        if (viewModel.verifySecurityAnswer(securityAnswer)) {
                                            currentStep = 3
                                        } else {
                                            // Mostrar error
                                            viewModel.setError("Respuesta incorrecta. Inténtalo de nuevo.")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = "Verificar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    3 -> {
                        // Paso 3: Crear nueva contraseña
                        Text(
                            text = "Crea una nueva contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nueva contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE53935),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedLabelColor = Color(0xFFE53935),
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
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
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.dp,
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE53935))
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = "Atrás",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                        if (newPassword == confirmPassword) {
                                            // Resetear contraseña (simulado)
                                            viewModel.resetPassword(email, newPassword)
                                            // Mostrar diálogo de éxito
                                            viewModel.markSuccess()
                                        } else {
                                            // Mostrar error
                                            viewModel.setError("Las contraseñas no coinciden")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = "Restablecer",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Botón para volver al login
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Volver a Iniciar Sesión",
                        color = Color(0xFFE53935),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
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
            
            // Diálogo de éxito
            if (viewModel.successState) {
                AlertDialog(
                    onDismissRequest = { /* No hacer nada */ },
                    title = { 
                        Text(
                            text = "¡Contraseña Restablecida!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF4CAF50)
                        ) 
                    },
                    text = { 
                        Text(
                            text = "Tu contraseña ha sido restablecida correctamente. Ahora puedes iniciar sesión con tu nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.clearSuccess()
                            onNavigateToLogin()
                        }) {
                            Text(
                                text = "Ir a Iniciar Sesión",
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
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int) {
    val isActive = step <= currentStep
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isActive) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = step.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
} 