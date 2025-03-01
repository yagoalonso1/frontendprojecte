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

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isError by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoginSuccessful by mutableStateOf(false)
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
                        isLoginSuccessful = true
                        token = loginResponse.accessToken
                        user = loginResponse.user
                        clearFields()
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
}