package com.example.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")
    var role by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isRegisterSuccessful by mutableStateOf(false)

    fun onRegisterClick() {
        errorMessage = ""

        if (!isValidEmail(email)) {
            errorMessage = "Por favor, ingresa un correo válido."
            return
        }

        if (password.length < 4) {
            errorMessage = "La contraseña debe tener al menos 4 caracteres."
            return
        }

        if (name.isEmpty()) {
            errorMessage = "El nombre no puede estar vacío."
            return
        }

        if (role.isEmpty()) {
            errorMessage = "Por favor, selecciona un rol."
            return
        }

        isLoading = true
        viewModelScope.launch {
            delay(2000)
            isRegisterSuccessful = true
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