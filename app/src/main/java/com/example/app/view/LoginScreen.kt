package com.example.app.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.app.util.GoogleAuthHelper
import com.example.app.R
import com.example.app.viewmodel.LoginViewModel
import com.example.app.viewmodel.TicketsViewModel
import com.example.app.viewmodel.ForgotPasswordViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import com.example.app.routes.Routes
import com.example.app.util.SessionManager
import com.example.app.routes.safeTokenDisplay
import com.example.app.ui.components.LanguageMenuButton
import com.example.app.ui.components.LanguageAwareText
import java.util.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    navController: NavController,
    viewModel: LoginViewModel = viewModel(),
    ticketsViewModel: TicketsViewModel = viewModel(
        factory = TicketsViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    // Contexto para manejar idiomas
    val context = LocalContext.current
    
    // Establecer el contexto en el ViewModel para acceder a los recursos
    LaunchedEffect(context) {
        viewModel.setContext(context)
    }
    
    // Cargar el idioma guardado (si existe)
    LaunchedEffect(Unit) {
        val savedLanguage = SessionManager.getUserLanguage()
        if (savedLanguage != null) {
            com.example.app.util.LocaleHelper.setLocale(context, savedLanguage)
        }
    }
    
    // Estados básicos
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isError = errorMessage != null
    val isLoginSuccessful = viewModel.isLoginSuccessful.collectAsState().value
    val shouldNavigateToParticipanteRegister = viewModel.shouldNavigateToParticipanteRegister.collectAsState().value
    
    // Estado para actualizar la UI cuando cambia el idioma
    val currentLanguageCode = remember { mutableStateOf(SessionManager.getUserLanguage() ?: "es") }
    
    // Configuración para Google Auth
    val googleAuthHelper = remember { GoogleAuthHelper(context) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = googleAuthHelper.handleSignInResult(task)
            account?.let { viewModel.handleGoogleSignInResult(it) }
        }
    }
    
    // Asegurar que la sesión esté limpia al abrir LoginScreen
    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "Comprobando y limpiando el estado de sesión")
        // Modificado para no eliminar preferencias de idioma
        SessionManager.clearSession()
        viewModel.resetState()
    }
    
    // Navegar cuando el login es exitoso o se debe crear un perfil de participante
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            Log.d("LoginScreen", "Login exitoso, navegando a eventos")
            navController.navigate(Routes.Eventos.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
            viewModel.resetLoginState()
        }
    }
    
    LaunchedEffect(shouldNavigateToParticipanteRegister) {
        if (shouldNavigateToParticipanteRegister) {
            Log.d("LoginScreen", "Navegando a registro de participante")
            navController.navigate(Routes.RegisterParticipante.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
            viewModel.resetNavigationState()
        }
    }
    
    LoginContent(
        isLoading = isLoading,
        isError = isError,
        errorMessage = errorMessage,
        viewModel = viewModel,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onGoogleSignIn = { launcher.launch(googleAuthHelper.getSignInIntent()) },
        onSelectLanguage = { langCode ->
            Log.d("LoginScreen", "Cambiando idioma a: $langCode")
            try {
                // Obtener la actividad actual
                val activity = context as? android.app.Activity
                if (activity != null) {
                    // Aplicar cambio de idioma y forzar actualización
                    com.example.app.util.LocaleHelper.forceLocaleUpdate(
                        activity,
                        langCode
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Error al cambiar idioma: ${e.message}")
            }
        }
    )
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String?,
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
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
                // Selector de idioma en la parte superior centrado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Texto explicativo
                    LanguageAwareText(
                        textId = R.string.language_selector_title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    
                    // Botón de selector de idioma
                    LanguageMenuButton(
                        onSelectLanguage = onSelectLanguage
                    )
                }
                
                // Logo de la app
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Título de login
                LanguageAwareText(
                    textId = R.string.login_title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    ),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                
                // Campo de email
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = { LanguageAwareText(textId = R.string.login_email_hint) },
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo de contraseña
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = { LanguageAwareText(textId = R.string.login_password_hint) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color.Gray
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
                
                // Enlace para recuperar contraseña
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    LanguageAwareText(
                        textId = R.string.login_forgot_password,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE53935),
                        modifier = Modifier.clickable { onNavigateToForgotPassword() }
                    )
                }
                
                // Botón de login
                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    LanguageAwareText(
                        textId = R.string.login_button,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                
                // Separador "o"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                    LanguageAwareText(
                        textId = R.string.login_or_divider,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }
                
                // Botón de Google
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.5f))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4285F4)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo_google),
                            contentDescription = "Google Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LanguageAwareText(
                            textId = R.string.login_with_google,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Enlace para crear cuenta
                LanguageAwareText(
                    textId = R.string.login_no_account,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón para crear cuenta
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE53935))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    LanguageAwareText(
                        textId = R.string.login_create_account,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Copyright
                LanguageAwareText(
                    textId = R.string.login_copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            if (isLoading) {
                LoadingOverlay()
            }
            
            if (isError) {
                ErrorDialog(
                    errorMessage = errorMessage,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
fun ErrorDialog(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LanguageAwareText(textId = R.string.error_connection) },
        text = { 
            if (errorMessage != null) {
                Text(text = errorMessage)
            } else {
                LanguageAwareText(textId = R.string.error_login_failed)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                LanguageAwareText(
                    textId = R.string.ok_button,
                    color = Color(0xFFE53935)
                )
            }
        },
        containerColor = Color.White,
        titleContentColor = Color(0xFFE53935),
        textContentColor = Color.Black.copy(alpha = 0.7f)
    )
}