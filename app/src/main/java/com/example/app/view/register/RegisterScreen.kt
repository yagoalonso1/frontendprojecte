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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.R
import com.example.app.viewmodel.RegisterViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import com.example.app.ui.components.LanguageAwareText
import com.example.app.util.LocaleHelper

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    // Estados para mostrar/ocultar contraseñas
    var showPasswordRequirements by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Validación de campos
    val nameValid = viewModel.name.isEmpty() || !viewModel.isNameError
    val apellido1Valid = viewModel.apellido1.isEmpty() || !viewModel.isApellido1Error
    val apellido2Valid = viewModel.apellido2.isEmpty() || !viewModel.isApellido2Error
    val emailValid = viewModel.email.isEmpty() || !viewModel.isEmailError
    val passwordValid = viewModel.password.isEmpty() || !viewModel.isPasswordError
    val confirmPasswordValid = viewModel.confirmPassword.isEmpty() || !viewModel.isConfirmPasswordError
    
    // Verificar si se puede activar los botones
    val allFieldsFilled = viewModel.name.isNotEmpty() && 
                         viewModel.apellido1.isNotEmpty() && 
                         viewModel.email.isNotEmpty() && 
                         viewModel.password.isNotEmpty() && 
                         viewModel.confirmPassword.isNotEmpty()
    
    val allFieldsValid = !viewModel.isNameError && 
                         !viewModel.isApellido1Error && 
                         !viewModel.isEmailError && 
                         !viewModel.isPasswordError && 
                         !viewModel.isConfirmPasswordError &&
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
                    text = stringResource(id = R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE53935),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Campos de formulario
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                        label = { LanguageAwareText(textId = R.string.register_name) },
                        isError = viewModel.isNameError,
                        supportingText = {
                            if (viewModel.isNameError) {
                                Text(
                                    text = viewModel.nameErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                        label = { LanguageAwareText(textId = R.string.register_lastname1) },
                        isError = viewModel.isApellido1Error,
                        supportingText = {
                            if (viewModel.isApellido1Error) {
                                Text(
                                    text = viewModel.apellido1ErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                        label = { LanguageAwareText(textId = R.string.register_lastname2) },
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
                        label = { LanguageAwareText(textId = R.string.register_email) },
                        isError = viewModel.isEmailError,
                        supportingText = {
                            if (viewModel.isEmailError) {
                                Text(
                                    text = viewModel.emailErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                        label = { LanguageAwareText(textId = R.string.register_password) },
                        isError = viewModel.isPasswordError,
                        supportingText = {
                            if (viewModel.isPasswordError) {
                                Text(
                                    text = viewModel.passwordErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                                        contentDescription = stringResource(id = R.string.register_password_requirements),
                                        tint = Color(0xFFE53935)
                                    )
                                }
                                // Botón para mostrar/ocultar contraseña
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (passwordVisible) stringResource(id = R.string.register_password) else stringResource(id = R.string.register_password),
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
                                LanguageAwareText(
                                    textId = R.string.register_password_requirements,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF333333),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                LanguageAwareText(
                                    textId = R.string.register_password_req1,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                LanguageAwareText(
                                    textId = R.string.register_password_req2,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                LanguageAwareText(
                                    textId = R.string.register_password_req3,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                LanguageAwareText(
                                    textId = R.string.register_password_req4,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                
                                LanguageAwareText(
                                    textId = R.string.register_password_req5,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { 
                            viewModel.confirmPassword = it
                            if (it.isNotEmpty()) {
                                viewModel.validateField("confirmPassword", it)
                            } else {
                                viewModel.isConfirmPasswordError = false
                            }
                        },
                        label = { LanguageAwareText(textId = R.string.register_confirm_password) },
                        isError = viewModel.isConfirmPasswordError,
                        supportingText = {
                            if (viewModel.isConfirmPasswordError) {
                                Text(
                                    text = viewModel.confirmPasswordErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (confirmPasswordVisible) stringResource(id = R.string.register_password) else stringResource(id = R.string.register_password),
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
                        text = stringResource(id = R.string.register_role_select),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.role = "organizador"
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
                            LanguageAwareText(
                                textId = R.string.register_role_organizer,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.role = "participante"
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
                            LanguageAwareText(
                                textId = R.string.register_role_participant,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Mensaje de ayuda si los botones están deshabilitados
                    if (!buttonsEnabled) {
                        LanguageAwareText(
                            textId = R.string.register_complete_fields,
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
                    LanguageAwareText(
                        textId = R.string.register_have_account,
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
    val errorMessageState by viewModel.errorMessage.collectAsState()
    if (viewModel.isError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { 
                LanguageAwareText(
                    textId = R.string.register_error_title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                ) 
            },
            text = { 
                androidx.compose.material3.Text(
                    text = errorMessageState ?: "",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    LanguageAwareText(
                        textId = R.string.ok_button,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
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