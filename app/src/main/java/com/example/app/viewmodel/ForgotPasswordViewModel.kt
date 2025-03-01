package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var isError by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successState by mutableStateOf(false)
    
    // Respuesta de seguridad correcta (en un caso real estaría en la base de datos)
    private val correctSecurityAnswer = "toby"
    
    fun verifyEmail(email: String) {
        viewModelScope.launch {
            isLoading = true
            // Simulamos verificación del email
            delay(1000)
            isLoading = false
            // En un caso real, verificaríamos si el email existe en la base de datos
        }
    }
    
    fun verifySecurityAnswer(answer: String): Boolean {
        // Simulamos verificación de la respuesta de seguridad
        // En un caso real, compararíamos con la respuesta almacenada en la base de datos
        return answer.trim().lowercase() == correctSecurityAnswer
    }
    
    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            isLoading = true
            // Simulamos el restablecimiento de la contraseña
            delay(1500)
            isLoading = false
            // En un caso real, actualizaríamos la contraseña en la base de datos
        }
    }
    
    fun setError(message: String) {
        isError = true
        errorMessage = message
    }
    
    fun clearError() {
        isError = false
        errorMessage = null
    }
    
    fun markSuccess() {
        successState = true
    }
    
    fun clearSuccess() {
        successState = false
    }
}