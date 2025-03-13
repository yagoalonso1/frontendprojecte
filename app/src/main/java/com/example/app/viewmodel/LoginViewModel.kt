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
                        if (loginResponse != null) {
                            // Guardar el token y la información del usuario
                            token = loginResponse.accessToken
                            user = loginResponse.user
                            clearFields()
                            _isLoginSuccessful.value = true
                            
                            // No llamamos a onLoginSuccess aquí porque navController podría no estar disponible
                            // La navegación se manejará en la vista a través del estado isLoginSuccessful
                        } else {
                            setError("Error desconocido durante el inicio de sesión")
                        }
                    } else {
                        // Intentar obtener el mensaje de error del cuerpo de la respuesta
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                            val message = errorResponse["message"] as? String ?: "Error en la comunicación con el servidor"
                            setError(message)
                        } catch (e: Exception) {
                            setError("Error en la comunicación con el servidor: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar errores
                withContext(Dispatchers.Main) {
                    isLoading = false
                    setError("Error de conexión: ${e.message}")
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
                // Obtener el token almacenado (asumiendo que lo tienes guardado)
                val token = "Bearer ${getUserToken()}"
                
                // Llamar al endpoint de logout
                val response = RetrofitClient.apiService.logoutUser(token)
                
                if (response.isSuccessful) {
                    // Limpiar datos locales
                    clearUserData()
                    _isLogoutSuccessful.value = true
                } else {
                    Log.e("LoginViewModel", "Error en logout: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error durante el logout", e)
            }
        }
    }

    private fun getUserToken(): String {
        // Implementa la lógica para obtener el token almacenado
        return "tu_token_almacenado"
    }

    private fun clearUserData() {
        // Implementa la lógica para limpiar los datos del usuario
    }

    private fun validateFields(): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            setError("Por favor, completa todos los campos")
            return false
        }
        return true
    }
}