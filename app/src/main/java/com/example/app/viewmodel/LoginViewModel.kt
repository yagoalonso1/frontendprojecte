package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.login.LoginRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var isError by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Cambiamos a StateFlow para poder observarlo en el NavHost
    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful = _isLoginSuccessful.asStateFlow()
    
    var token by mutableStateOf<String?>(null)
    var user by mutableStateOf<com.example.app.model.User?>(null)

    fun onLoginClick() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                if (email.isEmpty() || password.isEmpty()) {
                    setError("Por favor, completa todos los campos")
                    return@launch
                }

                // Crear objeto de petición
                val loginRequest = LoginRequest(
                    email = email,
                    password = password
                )

                // Llamar a la API
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.loginUser(loginRequest)
                }

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        token = loginResponse.accessToken
                        user = loginResponse.user
                        clearFields()
                        _isLoginSuccessful.value = true
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
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error durante el inicio de sesión", e)
                setError("Error durante el inicio de sesión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun clearFields() {
        email = ""
        password = ""
    }

    fun clearError() {
        isError = false
        errorMessage = null
    }

    fun setError(message: String) {
        isError = true
        errorMessage = message
    }

    // Función para resetear el estado de inicio de sesión (útil para cerrar sesión)
    fun resetLoginState() {
        _isLoginSuccessful.value = false
        token = null
        user = null
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            try {
                isLoading = true
                
                // Verificar si tenemos un token
                if (token == null) {
                    resetLoginState()
                    return@launch
                }
                
                // Llamar a la API para invalidar el token
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.logoutUser("Bearer $token")
                }
                
                // Independientemente de la respuesta, limpiamos el estado local
                resetLoginState()
                
                // Registramos el resultado para depuración
                if (response.isSuccessful) {
                    Log.d("LoginViewModel", "Logout exitoso")
                } else {
                    Log.e("LoginViewModel", "Error en logout: ${response.code()}")
                }
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error durante el logout", e)
                // Aún así, limpiamos el estado local
                resetLoginState()
            } finally {
                isLoading = false
            }
        }
    }
}