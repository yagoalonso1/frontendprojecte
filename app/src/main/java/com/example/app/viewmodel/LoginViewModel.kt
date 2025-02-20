package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isLoginSuccessful by mutableStateOf(false)

    fun onLoginClick() {
        errorMessage = ""

        // Validar correo
        if (!isValidEmail(email)) {
            errorMessage = "Por favor, ingresa un correo válido."
            return
        }

        // Validar contraseña
        if (password.isEmpty()) {
            errorMessage = "La contraseña no puede estar vacía."
            return
        }

        // Simulación de inicio de sesión
        isLoading = true
        viewModelScope.launch {
            delay(2000)

            // Ejemplo: Verificación sencilla de usuario y contraseña
            if (email == "test@example.com" && password == "1234") {
                isLoginSuccessful = true
            } else {
                errorMessage = "Correo o contraseña incorrectos."
            }
            isLoading = false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        )
        return emailPattern.matcher(email).matches()
    }
}