package com.example.app.view

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.routes.Routes
import com.example.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import com.example.app.routes.BottomNavigationBar
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.app.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.activity.ComponentActivity
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.ui.res.stringResource

// Colores consistentes con la app y la marca
private val primaryColor = Color(0xFFE53935)  // Rojo principal del logo usado en otras pantallas
private val secondaryDarkRed = Color(0xFF652C2D) // Tono más oscuro de rojo (#652c2d)
private val accentRed = Color(0xFFA53435) // Tono medio de rojo (#a53435)
private val backgroundColor = Color.White // Fondo blanco como en otras pantallas
private val textPrimaryColor = Color.Black // Texto negro para consistencia
private val textSecondaryColor = Color.Gray // Gris para textos secundarios, como en LoginScreen
private val surfaceColor = Color(0xFFF5F5F5)  // Gris muy claro para fondos de tarjetas
private val cardBackground = Color(0xFFFFFFFF) // Blanco puro para tarjetas
private val dividerColor = Color(0xFFE0E0E0) // Color para divisores

/**
 * Función auxiliar que proporciona una función para cambiar el idioma
 * sin depender de un contexto composable directo
 */
class LanguageManager(private val context: Context) {
    fun changeLanguage(langCode: String): String {
        // Aplicar el cambio de idioma
        val updatedContext = com.example.app.util.LocaleHelper.setLocale(context, langCode)
        
        // Guardar también en SessionManager para mantener la coherencia
        com.example.app.util.SessionManager.saveUserLanguage(langCode)
        
        // Obtener la clave del string apropiada para el mensaje según el idioma seleccionado
        val stringResId = when (langCode) {
            "ca" -> com.example.app.R.string.language_changed_to_catalan
            "en" -> com.example.app.R.string.language_changed_to_english
            else -> com.example.app.R.string.language_changed_to_spanish
        }
        
        // Obtener el mensaje traducido del contexto actualizado
        val message = updatedContext.getString(stringResId)
        
        // Forzar reinicio de la actividad para aplicar los cambios
        try {
            val activity = context as? android.app.Activity
            if (activity != null) {
                // Crear un intent para reiniciar la actividad
                val intent = activity.intent
                activity.finish()
                activity.startActivity(intent)
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageManager", "Error reiniciando actividad: ${e.message}")
        }
        
        return message
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileData = viewModel.profileData
    val isEditing = viewModel.isEditing
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    
    val successState = viewModel.isUpdateSuccessful.collectAsState(initial = false)
    val shouldNavigateToLogin = viewModel.shouldNavigateToLogin.collectAsState(initial = false)
    
    // Verificación de token inmediata al inicio del composable
    val hasValidToken = remember { mutableStateOf(true) }
    
    // Verificar token inmediatamente
    LaunchedEffect(Unit) {
        val token = com.example.app.util.SessionManager.getToken()
        if (token.isNullOrEmpty()) {
            hasValidToken.value = false
            navController.navigate(com.example.app.routes.Routes.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // Solo cargar el perfil si hay token válido
            viewModel.loadProfile()
        }
    }
    
    // Obtener el rol del usuario desde SessionManager para mostrar el menú correcto desde el inicio
    val initialUserRole = remember {
        com.example.app.util.SessionManager.getUserRole() ?: "participante"
    }
    
    // Navegar al login cuando la sesión ha expirado o cuando se cierra sesión
    LaunchedEffect(shouldNavigateToLogin.value) {
        if (shouldNavigateToLogin.value) {
            Log.d("ProfileScreen", "shouldNavigateToLogin es true, iniciando navegación a login")
            
            // Asegurarse de que la sesión esté limpia (esto debería ya estar hecho antes de este punto)
            if (com.example.app.util.SessionManager.getToken() != null) {
                Log.d("ProfileScreen", "Limpiando sesión antes de navegar")
            com.example.app.util.SessionManager.clearSession()
            }
            
            // Navegar a login limpiando el back stack completamente
            Log.d("ProfileScreen", "Navegando a la pantalla de login")
            navController.navigate(com.example.app.routes.Routes.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            Log.d("ProfileScreen", "Navegación a login completa")
            
            // Resetear el estado de navegación
            viewModel.resetShouldNavigateToLogin()
            Log.d("ProfileScreen", "shouldNavigateToLogin reiniciado a false")
        }
    }
    
    // Si no hay token válido, no mostramos nada (ya se está navegando a login)
    if (!hasValidToken.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = primaryColor
            )
        }
        return
    }
    
    // Mostrar snackbar en caso de éxito
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    
    // Obtener strings para evitar llamarlos dentro de LaunchedEffect
    val perfilActualizadoText = stringResource(id = R.string.perfil_actualizado)
    
    LaunchedEffect(successState.value) {
        if (successState.value) {
            showSnackbar = true
            snackbarMessage = perfilActualizadoText
            viewModel.resetUpdateState()
        }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }
    
    val avatarUrl = profileData?.avatar

    LaunchedEffect(avatarUrl) {
        // Añadir logs para depurar el avatar
        Log.d("ProfileScreen", "Avatar URL en ProfileScreen: $avatarUrl")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.perfil_titulo),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = 1.sp
                        ),
                        color = primaryColor
                    )
                },
                actions = {
                    val context = LocalContext.current
                    val languageManager = remember { LanguageManager(context) }
                    
                    // Botón de selección de idioma (globo terráqueo)
                    com.example.app.ui.components.LanguageMenuButton(
                        onSelectLanguage = { langCode ->
                            // Usar el gestor de idiomas para cambiar el idioma
                            val message = languageManager.changeLanguage(langCode)
                            showSnackbar = true
                            snackbarMessage = message
                        }
                    )
                    
                    // Espacio entre botones
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Botón de editar perfil
                    if (!isEditing && profileData != null) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(
                                Icons.Default.Edit, 
                                contentDescription = stringResource(id = R.string.perfil_editar),
                                tint = primaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = primaryColor
                )
            )
        },
        bottomBar = {
            // Usar el rol guardado en SessionManager hasta que se cargue el perfil
            val userRole = viewModel.profileData?.role ?: initialUserRole
            Log.d("ProfileScreen", "Mostrando barra de navegación con rol: $userRole")
            BottomNavigationBar(
                navController = navController,
                userRole = userRole
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = secondaryDarkRed,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = backgroundColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        errorMessage, 
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Si es error de sesión expirada, mostrar botón para ir al login
                    if (errorMessage.contains("sesión ha expirado", ignoreCase = true)) {
                        Button(
                            onClick = { viewModel.navigateToLogin() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(id = R.string.perfil_iniciar_sesion), color = Color.White)
                        }
                    } else {
                        // Para otros errores, mostrar botón de reintentar
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(id = R.string.perfil_reintentar), color = Color.White)
                        }
                    }
                }
            } else if (profileData != null) {
                if (isEditing) {
                    ProfileEditMode(viewModel = viewModel)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Card de perfil principal con avatar y nombre
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar recibido del backend
                                if (!avatarUrl.isNullOrEmpty()) {
                                    Log.d("ProfileScreen", "Cargando avatar desde URL: $avatarUrl")
                                    
                                    // Cargar directamente la URL del avatar proporcionada por el backend
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar del usuario",
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            CircularProgressIndicator(
                                                color = primaryColor,
                                                modifier = Modifier.padding(40.dp)
                                            )
                                        },
                                        error = {
                                            Log.e("ProfileScreen", "Error al cargar avatar desde URL: $avatarUrl")
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = CircleShape,
                                                color = primaryColor.copy(alpha = 0.15f),
                                                border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
                                            ) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = "Avatar por defecto",
                                                    modifier = Modifier
                                                        .padding(24.dp)
                                                        .size(64.dp),
                                                    tint = primaryColor
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                                    )
                                } else {
                                    Log.w("ProfileScreen", "Avatar URL nula o vacía, mostrando avatar por defecto")
                                    // Avatar por defecto con icono de persona
                                    Surface(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        shape = CircleShape,
                                        color = primaryColor.copy(alpha = 0.15f),
                                        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Avatar por defecto",
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .size(64.dp),
                                            tint = primaryColor
                                        )
                                    }
                                }
                                
                                // Nombre completo
                                Text(
                                    text = "${profileData.nombre ?: ""} ${profileData.apellido1 ?: ""} ${profileData.apellido2 ?: ""}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    modifier = Modifier.padding(top = 16.dp),
                                    textAlign = TextAlign.Center,
                                    color = textPrimaryColor
                                )
                                
                                // Rol
                                Text(
                                    text = when(profileData.role) {
                                        "organizador" -> stringResource(id = R.string.perfil_rol_organizador)
                                        "participante" -> stringResource(id = R.string.perfil_rol_participante)
                                        else -> profileData.role ?: ""
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryColor,
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                // Email
                                Text(
                                    text = profileData.email ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondaryColor,
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Card con información personal
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.perfil_informacion_personal),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = secondaryDarkRed,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                // Mostrar campos según el rol
                                if (profileData.role == "participante") {
                                    ProfileFieldRow(stringResource(id = R.string.perfil_dni), profileData.dni ?: "No especificado")
                                    HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    ProfileFieldRow(stringResource(id = R.string.perfil_telefono), profileData.telefono ?: "No especificado")
                                } else if (profileData.role == "organizador") {
                                    ProfileFieldRow(stringResource(id = R.string.perfil_nombre_organizacion), profileData.nombreOrganizacion ?: "No especificado")
                                    HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    ProfileFieldRow(stringResource(id = R.string.perfil_telefono_contacto), profileData.telefonoContacto ?: "No especificado")
                                }
                            }
                        }
                        
                        // Botones de acción
                        ActionsButtonsSection(navController, viewModel, profileData)
                    }
                }
            } else {
                Text(
                    "No se pudo cargar la información del perfil",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = textPrimaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewMode(
    profileData: com.example.app.model.ProfileData,
    navController: NavController,
    viewModel: ProfileViewModel
) {
    // Variables de texto
    val eliminarCuentaTitulo = stringResource(id = R.string.eliminar_cuenta_titulo)
    val eliminarCuentaBoton = stringResource(id = R.string.eliminar_cuenta_boton)
    val eliminarCuentaAdvertencia = stringResource(id = R.string.eliminar_cuenta_advertencia)
    val eliminarCuentaEliminando = stringResource(id = R.string.eliminar_cuenta_eliminando)
    val datosPersonales = stringResource(id = R.string.eliminar_cuenta_datos_personales)
    val historial = stringResource(id = R.string.eliminar_cuenta_historial)
    val eventosFavoritos = stringResource(id = R.string.eliminar_cuenta_eventos_favoritos)
    val organizadoresFavoritos = stringResource(id = R.string.eliminar_cuenta_organizadores_favoritos)
    val eventosCreados = stringResource(id = R.string.eliminar_cuenta_eventos_creados)
    val tiposEntrada = stringResource(id = R.string.eliminar_cuenta_tipos_entrada)
    val advertenciaFinal = stringResource(id = R.string.eliminar_cuenta_advertencia_final)
    val contrasenaTxt = stringResource(id = R.string.eliminar_cuenta_contrasena)
    val ocultarTxt = stringResource(id = R.string.eliminar_cuenta_ocultar)
    val mostrarTxt = stringResource(id = R.string.eliminar_cuenta_mostrar)
    val cancelarTxt = stringResource(id = R.string.perfil_cancelar)
    val confirmarTxt = stringResource(id = R.string.eliminar_cuenta_confirmar)
    val exitoTitulo = stringResource(id = R.string.eliminar_cuenta_exito_titulo)
    val exitoMensaje = stringResource(id = R.string.eliminar_cuenta_exito_mensaje)
    val exitoBoton = stringResource(id = R.string.eliminar_cuenta_exito_boton)
    
    // Estado para manejar la visibilidad del diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Estado para el campo de contraseña
    var password by remember { mutableStateOf("") }
    // Estado para controlar si la contraseña es visible
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Monitor del estado de eliminación exitosa
    val deleteSuccessState = viewModel.isDeleteAccountSuccessful.collectAsState(initial = false)
    val showSuccessDialog = viewModel.showDeleteSuccessDialog.collectAsState(initial = false)
    
    // Verificar si hay token válido y redirigir si es necesario
    LaunchedEffect(Unit) {
        val token = com.example.app.util.SessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Log.d("ProfileViewMode", "No hay token válido, redirigiendo a login")
            navController.navigate(com.example.app.routes.Routes.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    // Mostrar un snackbar cuando la eliminación de cuenta sea exitosa
    LaunchedEffect(deleteSuccessState.value) {
        if (deleteSuccessState.value) {
            // Ya no hacemos nada aquí porque ahora se muestra un diálogo
            viewModel.resetDeleteAccountState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de perfil con icono
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "${profileData.nombre ?: ""} ${profileData.apellido1 ?: ""} ${profileData.apellido2 ?: ""}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
                Text(
                    text = profileData.tipoUsuario ?: profileData.role ?: "",
                    fontSize = 16.sp,
                    color = primaryColor
                )
            }
        }
        
        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Información personal
        val infoPersonalTitulo = stringResource(id = R.string.perfil_informacion_personal)
        ProfileSection(title = infoPersonalTitulo) {
            val emailLabel = stringResource(id = R.string.perfil_email)
            ProfileField(emailLabel, profileData.email ?: "")
            
            // Mostrar campos según el rol
            if (profileData.role == "participante") {
                val dniLabel = stringResource(id = R.string.perfil_dni)
                val telefonoLabel = stringResource(id = R.string.perfil_telefono)
                ProfileField(dniLabel, profileData.dni ?: "")
                ProfileField(telefonoLabel, profileData.telefono ?: "")
            } else if (profileData.role == "organizador") {
                val nombreOrgLabel = stringResource(id = R.string.perfil_nombre_organizacion)
                val telContactoLabel = stringResource(id = R.string.perfil_telefono_contacto)
                ProfileField(nombreOrgLabel, profileData.nombreOrganizacion ?: "")
                ProfileField(telContactoLabel, profileData.telefonoContacto ?: "")
            }
        }
        
        // Espaciador para separar el botón de cerrar sesión
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sección de botones de acción
        ActionsButtonsSection(navController, viewModel, profileData)
    }
    
    // Diálogo de éxito cuando la cuenta se ha eliminado correctamente
    if (showSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = exitoTitulo,
                        tint = Color.Green,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = exitoTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = exitoMensaje,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            viewModel.confirmDeleteSuccess()
                            navController.navigate(Routes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(exitoBoton)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(primaryColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textPrimaryColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditMode(viewModel: ProfileViewModel) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.perfil_editar),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Campos generales
        OutlinedTextField(
            value = viewModel.nombre,
            onValueChange = { viewModel.nombre = it },
            label = { Text(stringResource(id = R.string.perfil_nombre)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = viewModel.apellido1,
            onValueChange = { viewModel.apellido1 = it },
            label = { Text(stringResource(id = R.string.perfil_primer_apellido)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = viewModel.apellido2,
            onValueChange = { viewModel.apellido2 = it },
            label = { Text(stringResource(id = R.string.perfil_segundo_apellido)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        // Email no editable
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { },
            label = { Text(stringResource(id = R.string.perfil_email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = Color.Gray,
                disabledLabelColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
        
        // Campos específicos según rol
        val profileData = viewModel.profileData
        if (profileData?.role == "participante") {
            OutlinedTextField(
                value = viewModel.dni,
                onValueChange = { viewModel.dni = it },
                label = { Text(stringResource(id = R.string.perfil_dni)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            OutlinedTextField(
                value = viewModel.telefono,
                onValueChange = { viewModel.telefono = it },
                label = { Text(stringResource(id = R.string.perfil_telefono)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
        } else if (profileData?.role == "organizador") {
            OutlinedTextField(
                value = viewModel.nombreOrganizacion,
                onValueChange = { viewModel.nombreOrganizacion = it },
                label = { Text(stringResource(id = R.string.perfil_nombre_organizacion)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            OutlinedTextField(
                value = viewModel.telefonoContacto,
                onValueChange = { viewModel.telefonoContacto = it },
                label = { Text(stringResource(id = R.string.perfil_telefono_contacto)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        
        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelEditing() },
                modifier = Modifier.weight(1f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(primaryColor)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(id = R.string.perfil_cancelar))
            }
            
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.perfil_guardar))
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = textSecondaryColor
        )
        
        Text(
            text = value.ifEmpty { stringResource(id = R.string.perfil_no_especificado) },
            fontSize = 16.sp,
            color = textPrimaryColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textSecondaryColor
            )
            
            Text(
                text = value.ifEmpty { stringResource(id = R.string.perfil_no_especificado) },
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsButtonsSection(
    navController: NavController,
    viewModel: ProfileViewModel,
    profileData: com.example.app.model.ProfileData
) {
    // Variables de texto
    val eliminarCuentaTitulo = stringResource(id = R.string.eliminar_cuenta_titulo)
    val eliminarCuentaBoton = stringResource(id = R.string.eliminar_cuenta_boton)
    val eliminarCuentaAdvertencia = stringResource(id = R.string.eliminar_cuenta_advertencia)
    val eliminarCuentaEliminando = stringResource(id = R.string.eliminar_cuenta_eliminando)
    val datosPersonales = stringResource(id = R.string.eliminar_cuenta_datos_personales)
    val historial = stringResource(id = R.string.eliminar_cuenta_historial)
    val eventosFavoritos = stringResource(id = R.string.eliminar_cuenta_eventos_favoritos)
    val organizadoresFavoritos = stringResource(id = R.string.eliminar_cuenta_organizadores_favoritos)
    val eventosCreados = stringResource(id = R.string.eliminar_cuenta_eventos_creados)
    val tiposEntrada = stringResource(id = R.string.eliminar_cuenta_tipos_entrada)
    val advertenciaFinal = stringResource(id = R.string.eliminar_cuenta_advertencia_final)
    val contrasenaTxt = stringResource(id = R.string.eliminar_cuenta_contrasena)
    val ocultarTxt = stringResource(id = R.string.eliminar_cuenta_ocultar)
    val mostrarTxt = stringResource(id = R.string.eliminar_cuenta_mostrar)
    val cancelarTxt = stringResource(id = R.string.perfil_cancelar)
    val confirmarTxt = stringResource(id = R.string.eliminar_cuenta_confirmar)
    val exitoTitulo = stringResource(id = R.string.eliminar_cuenta_exito_titulo)
    val exitoMensaje = stringResource(id = R.string.eliminar_cuenta_exito_mensaje)
    val exitoBoton = stringResource(id = R.string.eliminar_cuenta_exito_boton)
    
    // Variable para controlar diálogo de cambio de contraseña
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    // Variables para el diálogo de eliminar cuenta
    var showDeleteDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Monitor del estado de eliminación exitosa
    val deleteSuccessState = viewModel.isDeleteAccountSuccessful.collectAsState(initial = false)
    val showSuccessDialog = viewModel.showDeleteSuccessDialog.collectAsState(initial = false)
    
    // Botón de historial de compras (solo para participantes)
    if (profileData.role == "participante") {
        Button(
            onClick = { 
                navController.navigate(com.example.app.routes.Routes.HistorialCompras.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentRed, // Tono medio de rojo para distinguir
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = stringResource(id = R.string.historial_compras_titulo),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.historial_compras_titulo),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Botón de cambiar contraseña
    Button(
        onClick = { showChangePasswordDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = secondaryDarkRed, // Tono oscuro para botones importantes
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = stringResource(id = R.string.cambiar_contrasena_titulo),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(id = R.string.cambiar_contrasena_titulo).uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    // Mostrar el diálogo de cambio de contraseña si es necesario
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            viewModel = viewModel,
            onDismiss = { showChangePasswordDialog = false }
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Botón de cerrar sesión
    Button(
        onClick = { viewModel.logout() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Logout,
            contentDescription = stringResource(id = R.string.perfil_cerrar_sesion),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(id = R.string.perfil_cerrar_sesion).uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Botón de eliminar cuenta
    Button(
        onClick = { 
            // Verificar token antes de mostrar el diálogo
            val token = com.example.app.util.SessionManager.getToken()
            if (token.isNullOrEmpty()) {
                // Si no hay token, navegar directamente a login
                navController.navigate(com.example.app.routes.Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                // Si hay token, mostrar el diálogo
                showDeleteDialog = true
                // Limpiar contraseña en caso de que haya quedado de algún intento anterior
                password = ""
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFAD2A2A), // Rojo más oscuro para acciones peligrosas
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            Icons.Filled.Delete,
            contentDescription = eliminarCuentaTitulo,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            eliminarCuentaBoton,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    // Espacio al final para evitar que el contenido quede pegado a la barra inferior
    Spacer(modifier = Modifier.height(24.dp))
    
    // Diálogo de confirmación para eliminar cuenta
    if (showDeleteDialog) {
        val isLoading = viewModel.isLoading
        val errorMessage = viewModel.errorMessage
        
        // Verificación de token antes de mostrar el diálogo
        DisposableEffect(Unit) {
            val token = com.example.app.util.SessionManager.getToken()
            if (token.isNullOrEmpty()) {
                // Si no hay token, cerrar el diálogo y navegar a login
                showDeleteDialog = false
                navController.navigate(com.example.app.routes.Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            
            onDispose { }
        }
        
        AlertDialog(
            onDismissRequest = { 
                if (!isLoading) {
                    showDeleteDialog = false
                    password = ""
                    viewModel.clearError()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !isLoading,
                dismissOnClickOutside = !isLoading
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título con icono
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = eliminarCuentaTitulo,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = eliminarCuentaTitulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = primaryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading) {
                        // Contenido del diálogo cuando está cargando
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            eliminarCuentaEliminando,
                            fontSize = 14.sp,
                            color = textSecondaryColor,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Contenido del diálogo normal
                        Text(
                            eliminarCuentaAdvertencia,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = textPrimaryColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lista de lo que se eliminará
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            BulletPoint(datosPersonales)
                            BulletPoint(historial)
                            if (profileData.role == "participante") {
                                BulletPoint(eventosFavoritos)
                                BulletPoint(organizadoresFavoritos)
                            } else {
                                BulletPoint(eventosCreados)
                                BulletPoint(tiposEntrada)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            advertenciaFinal,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Si hay un mensaje de error, mostrarlo
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Campo de contraseña
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(contrasenaTxt) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) ocultarTxt else mostrarTxt
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = primaryColor,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón Cancelar
                            OutlinedButton(
                                onClick = { 
                                    showDeleteDialog = false
                                    password = ""
                                    viewModel.clearError()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                ),
                                border = BorderStroke(1.dp, primaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(cancelarTxt)
                            }
                            
                            // Botón Confirmar
                            Button(
                                onClick = {
                                    viewModel.deleteAccount(password)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White,
                                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = password.isNotBlank()
                            ) {
                                Text(confirmarTxt)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de éxito cuando la cuenta se ha eliminado correctamente
    if (showSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = exitoTitulo,
                        tint = Color.Green,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = exitoTitulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = exitoMensaje,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            viewModel.confirmDeleteSuccess()
                            navController.navigate(Routes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(exitoBoton)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val isChangingPassword = viewModel.isChangingPassword
    val isPasswordChangeSuccessful by viewModel.isPasswordChangeSuccessful.collectAsState()
    val errorMessage = viewModel.errorMessage
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Para mostrar u ocultar las contraseñas
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Resetear el estado si el diálogo se cierra
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPasswordChangeState()
        }
    }
    
    // Si el cambio fue exitoso, cerrar automáticamente después de un tiempo
    LaunchedEffect(isPasswordChangeSuccessful) {
        if (isPasswordChangeSuccessful) {
            delay(1500)
            onDismiss()
        }
    }
    
    Dialog(
        onDismissRequest = { 
            if (!isChangingPassword) onDismiss() 
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = stringResource(id = R.string.cambiar_contrasena_titulo),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                if (isChangingPassword) {
                    // Pantalla de carga
                    CircularProgressIndicator(color = primaryColor)
                } else if (isPasswordChangeSuccessful) {
                    // Mensaje de éxito
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(id = R.string.cambiar_contrasena_exito),
                        tint = primaryColor,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(id = R.string.cambiar_contrasena_success),
                        textAlign = TextAlign.Center,
                        color = textPrimaryColor
                    )
                } else {
                    // Campos de entrada
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(stringResource(id = R.string.cambiar_contrasena_actual)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (currentPasswordVisible) 
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) 
                                        Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (currentPasswordVisible) 
                                        stringResource(id = R.string.cambiar_contrasena_ocultar) else stringResource(id = R.string.cambiar_contrasena_mostrar),
                                    tint = primaryColor
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(id = R.string.cambiar_contrasena_nueva)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) 
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) 
                                        Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (newPasswordVisible) 
                                        stringResource(id = R.string.cambiar_contrasena_ocultar) else stringResource(id = R.string.cambiar_contrasena_mostrar),
                                    tint = primaryColor
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(id = R.string.cambiar_contrasena_confirmar)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) 
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) 
                                        Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) 
                                        stringResource(id = R.string.cambiar_contrasena_ocultar) else stringResource(id = R.string.cambiar_contrasena_mostrar),
                                    tint = primaryColor
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    
                    // Mostrar mensaje de error si hay alguno
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = primaryColor,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botón Cancelar
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.cambiar_contrasena_boton_cancelar),
                                color = primaryColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        
                        // Botón Cambiar
                        Button(
                            onClick = {
                                viewModel.changePassword(
                                    currentPassword,
                                    newPassword,
                                    confirmPassword
                                )
                            },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = currentPassword.isNotEmpty() && 
                                      newPassword.isNotEmpty() && 
                                      confirmPassword.isNotEmpty()
                        ) {
                            Text(
                                text = stringResource(id = R.string.cambiar_contrasena_boton_cambiar),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
} 