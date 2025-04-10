package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.app.model.login.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.example.app.util.SessionManager

class LoginViewModel : ViewModel() {
    // Campos del formulario
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    
    // Estados de la UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Estado del login
    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful = _isLoginSuccessful.asStateFlow()
    
    // Datos de sesión
    var token by mutableStateOf<String?>(null)
    var user by mutableStateOf<com.example.app.model.User?>(null)

    // Estado del logout
    private val _isLogoutSuccessful = MutableStateFlow(false)
    val isLogoutSuccessful = _isLogoutSuccessful.asStateFlow()

    fun onLoginClick() {
        // Validar campos
        if (!validateFields()) {
            return
        }

        // Mostrar indicador de carga
        isLoading = true

        viewModelScope.launch {
            try {
                // Realizar la solicitud de inicio de sesión
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.loginUser(LoginRequest(email, password))
                }

                // Procesar la respuesta
                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Log.d("LoginViewModel", "Respuesta del servidor recibida: $loginResponse")
                        Log.d("LoginViewModel", "Código de respuesta: ${response.code()}")
                        
                        if (loginResponse != null) {
                            Log.d("LoginViewModel", "Verificando token...")
                            
                            try {
                                // Verificar si el usuario existe en la respuesta
                                if (loginResponse.user == null) {
                                    setError("La respuesta no contiene información de usuario")
                                    Log.e("LoginViewModel", "Usuario es null en la respuesta")
                                    return@withContext
                                }
                                
                                // Intentar obtener el token de donde esté disponible
                                val accessToken = loginResponse.token ?: loginResponse.accessToken
                                
                                // Validar que obtuvimos un token
                                if (accessToken.isNullOrBlank()) {
                                    setError("No se recibió un token válido del servidor")
                                    Log.e("LoginViewModel", "No se pudo obtener un token válido")
                                    return@withContext
                                }
                                
                                // Guardar el token y la información del usuario
                                token = accessToken
                                user = loginResponse.user
                                
                                Log.d("LoginViewModel", "Login exitoso")
                                Log.d("LoginViewModel", "Token: $token")
                                Log.d("LoginViewModel", "Usuario: ${user?.email}")
                                
                                // Determinar el rol de usuario (con manejo seguro de nulos)
                                val userRole = when {
                                    loginResponse.role?.isNotBlank() == true -> loginResponse.role
                                    loginResponse.userRole?.isNotBlank() == true -> loginResponse.userRole
                                    loginResponse.userRoleAlt?.isNotBlank() == true -> loginResponse.userRoleAlt
                                    else -> "participante" // valor por defecto
                                }
                                Log.d("LoginViewModel", "Rol determinado: $userRole")
                                
                                // Guardar en SessionManager (con manejo seguro de excepciones)
                                try {
                                    if (!SessionManager.isInitialized()) {
                                        Log.w("LoginViewModel", "SessionManager no está inicializado, no se guardarán los datos de sesión")
                                        // Continuamos con el login aunque no se guarden los datos
                                    } else {
                                        SessionManager.saveToken(accessToken)
                                        SessionManager.saveUserRole(userRole)
                                        
                                        // Verificar que se guardó correctamente
                                        val savedToken = SessionManager.getToken()
                                        val savedRole = SessionManager.getUserRole()
                                        Log.d("LoginViewModel", "Token guardado: $savedToken")
                                        Log.d("LoginViewModel", "Rol guardado: $savedRole")
                                    }
                                    
                                    // Siempre continuamos con el login
                                    clearFields()
                                    _isLoginSuccessful.value = true
                                } catch (e: Exception) {
                                    Log.e("LoginViewModel", "Error al guardar en SessionManager: ${e.message}")
                                    // No bloqueamos el login aunque haya errores en el guardado
                                    clearFields()
                                    _isLoginSuccessful.value = true
                                }
                            } catch (e: Exception) {
                                Log.e("LoginViewModel", "Error al procesar la respuesta: ${e.message}", e)
                                setError("Error al procesar la respuesta: ${e.message ?: "Error desconocido"}")
                                return@withContext
                            }
                        } else {
                            setError("Error desconocido durante el inicio de sesión")
                        }
                    } else {
                        // Intentar obtener el mensaje de error del cuerpo de la respuesta
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (errorBody != null) {
                                val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                val message = errorResponse["message"] as? String ?: "Error en la comunicación con el servidor"
                                setError(message)
                            } else {
                                setError("Error en la comunicación con el servidor: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            setError("Error en la comunicación con el servidor: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar errores
                withContext(Dispatchers.Main) {
                    isLoading = false
                    setError("Error de conexión: ${e.message ?: "Error desconocido"}")
                    Log.e("LoginViewModel", "Excepción durante el login", e)
                }
            }
        }
    }

    fun clearFields() {
        email = ""
        password = ""
    }

    fun setError(message: String) {
        errorMessage = message
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetState() {
        _isLoginSuccessful.value = false
        _isLogoutSuccessful.value = false
        clearError()
        // No limpiar email/password para facilitar reautenticación
    }

    fun onForgotPasswordClick() {
        // No hacer nada aquí, la navegación se maneja en la vista
    }

    fun onRegisterClick() {
        // No hacer nada aquí, la navegación se maneja en la vista
    }

    fun onLoginSuccess(userType: String) {
        viewModelScope.launch {
            try {
                // No hacer nada aquí, la navegación se maneja en la vista
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al navegar después del inicio de sesión", e)
                setError("Error al navegar: ${e.message}")
            }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            try {
                isLoading = true
                
                // Simular una petición de cierre de sesión
                delay(500)
                
                // Limpiar datos de sesión
                token = null
                user = null
                _isLoginSuccessful.value = false
                _isLogoutSuccessful.value = true
                
                Log.d("LoginViewModel", "Sesión cerrada correctamente")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al cerrar sesión", e)
                setError("Error al cerrar sesión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Función para resetear el estado de cierre de sesión
    fun resetLogoutState() {
        _isLogoutSuccessful.value = false
    }

    // Función que será llamada desde el HomeScreen
    fun performLogout() {
        viewModelScope.launch {
            try {
                isLoading = true
                
                // Obtener el token almacenado
                val token = SessionManager.getToken()
                
                if (token == null) {
                    Log.e("LoginViewModel", "No hay token para logout")
                    // Si no hay token, simplemente limpiamos los datos locales
                    clearUserData()
                    _isLogoutSuccessful.value = true
                    return@launch
                }
                
                // Llamar al endpoint de logout con el token
                try {
                    val response = RetrofitClient.apiService.logoutUser("Bearer $token")
                    
                    if (response.isSuccessful) {
                        Log.d("LoginViewModel", "Logout exitoso en el servidor")
                    } else {
                        Log.e("LoginViewModel", "Error en logout: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Excepción en logout: ${e.message}")
                }
                
                // Siempre limpiamos los datos locales, incluso si falla el logout en el servidor
                clearUserData()
                _isLogoutSuccessful.value = true
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error durante el logout", e)
                // Aún así intentamos limpiar los datos locales
                clearUserData()
                _isLogoutSuccessful.value = true
            } finally {
                isLoading = false
            }
        }
    }

    private fun clearUserData() {
        // Limpiar datos locales
        SessionManager.clearSession()
        token = null
        user = null
        _isLoginSuccessful.value = false
    }

    private fun validateFields(): Boolean {
        if (email.isEmpty()) {
            setError("Por favor, introduce tu correo electrónico")
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError("Por favor, introduce un correo electrónico válido")
            return false
        }
        
        if (password.isEmpty()) {
            setError("Por favor, introduce tu contraseña")
            return false
        }
        
        // Limpiar error si todo está bien
        clearError()
        return true
    }
}