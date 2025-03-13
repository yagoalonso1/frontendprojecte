package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import com.example.app.api.RetrofitClient
import com.example.app.model.register.RegisterRequest
import com.example.app.model.register.RegisterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.example.app.model.ErrorResponse

class RegisterViewModel : ViewModel() {
    // Datos básicos
    var name by mutableStateOf("")
    var apellido1 by mutableStateOf("")
    var apellido2 by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var role by mutableStateOf("participante")

    // Datos de organizador
    var nombreOrganizacion by mutableStateOf("")
    var telefonoContacto by mutableStateOf("")

    // Datos de participante
    var dni by mutableStateOf("")
    var telefono by mutableStateOf("")

    // Estados de error básicos
    var isNameError by mutableStateOf(false)
    var isApellido1Error by mutableStateOf(false)
    var isApellido2Error by mutableStateOf(false)
    var isEmailError by mutableStateOf(false)
    var isPasswordError by mutableStateOf(false)
    var isConfirmPasswordError by mutableStateOf(false)

    // Estados de error organizador
    var isNombreOrganizacionError by mutableStateOf(false)
    var isTelefonoContactoError by mutableStateOf(false)

    // Estados de error participante
    var isDniError by mutableStateOf(false)
    var isTelefonoError by mutableStateOf(false)

    // Mensajes de error
    var nameErrorMessage by mutableStateOf("")
    var apellido1ErrorMessage by mutableStateOf("")
    var apellido2ErrorMessage by mutableStateOf("")
    var emailErrorMessage by mutableStateOf("")
    var passwordErrorMessage by mutableStateOf("")
    var confirmPasswordErrorMessage by mutableStateOf("")
    var nombreOrganizacionErrorMessage by mutableStateOf("")
    var telefonoContactoErrorMessage by mutableStateOf("")
    var dniErrorMessage by mutableStateOf("")
    var telefonoErrorMessage by mutableStateOf("")

    // Estados de la UI
    var isLoading by mutableStateOf(false)
    private val _isRegisterSuccessful = MutableStateFlow(false)
    val isRegisterSuccessful: StateFlow<Boolean> = _isRegisterSuccessful

    // Estado de error
    var isError by mutableStateOf(false)
        private set

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Mensajes de debug
    private val _debugMessage = MutableStateFlow<String>("")
    val debugMessage: StateFlow<String> = _debugMessage

    // Patrones de validación
    private val nameRegex = "^[A-Za-zÁ-ÿ\\s]{2,}$"
    private val dniRegex = "^[0-9]{8}[A-Z]$"
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    private val phoneRegex = "^[0-9]{9}$"
    private val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[.@#$%^&+=!*()_\\-])(?=\\S+$).{8,}$"

    // Funciones de formato
    fun formatName(input: String): String {
        return input.trim().split(" ").joinToString(" ") { 
            it.replaceFirstChar { char -> char.uppercase() }
        }
    }

    // Validaciones
    fun isValidName(name: String): Boolean {
        return name.matches(Regex(nameRegex))
    }

    fun isValidEmail(email: String): Boolean {
        return Pattern.matches(emailRegex, email)
    }

    fun isValidDNI(dni: String): Boolean {
        if (!dni.matches(Regex(dniRegex))) return false
        
        val letters = "TRWAGMYFPDXBNJZSQVHLCKE"
        val number = dni.substring(0, 8).toInt()
        val letter = dni.last()
        
        return letters[number % 23] == letter
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex(phoneRegex))
    }

    fun isValidPassword(password: String): Boolean {
        // Verificar longitud mínima
        if (password.length < 8) return false
        
        // Verificar que contiene al menos un dígito
        if (!password.any { it.isDigit() }) return false
        
        // Verificar que contiene al menos una letra minúscula
        if (!password.any { it.isLowerCase() }) return false
        
        // Verificar que contiene al menos una letra mayúscula
        if (!password.any { it.isUpperCase() }) return false
        
        // Verificar que contiene al menos un carácter especial
        val specialChars = "!@#$%^&*()_-+={}[]|:;'<>,.?/~`\\"
        if (!password.any { it in specialChars || it == '.' }) return false
        
        // Verificar que no contiene espacios
        if (password.contains(" ")) return false
        
        return true
    }

    fun doPasswordsMatch(): Boolean {
        return password == confirmPassword && password.isNotEmpty()
    }

    fun isValidNombreOrganizacion(nombre: String): Boolean {
        // Permitir espacios en el nombre de la organización
        // Solo verificar que no esté vacío y tenga al menos 3 caracteres en total
        return nombre.trim().isNotEmpty() && nombre.trim().length >= 3
    }

    // Validación de campos
    fun validateField(field: String, value: String) {
        when (field) {
            "name" -> isNameError = !isValidName(value)
            "apellido1" -> isApellido1Error = !isValidName(value)
            "apellido2" -> isApellido2Error = !isValidName(value)
            "email" -> isEmailError = !isValidEmail(value)
            "dni" -> isDniError = !isValidDNI(value)
            "telefono" -> isTelefonoError = !isValidPhone(value)
            "password" -> isPasswordError = !isValidPassword(value)
            "confirmPassword" -> isConfirmPasswordError = !doPasswordsMatch()
            "nombreOrganizacion" -> isNombreOrganizacionError = !isValidNombreOrganizacion(value)
            "telefonoContacto" -> isTelefonoContactoError = !isValidPhone(value)
        }
    }

    fun validateAllFields(): Boolean {
        var isValid = true

        // Validar campos básicos
        if (name.isBlank()) {
            isNameError = true
            nameErrorMessage = "El nombre es requerido"
            isValid = false
        } else {
            isNameError = false
            nameErrorMessage = ""
        }

        if (apellido1.isBlank()) {
            isApellido1Error = true
            apellido1ErrorMessage = "El primer apellido es requerido"
            isValid = false
        } else {
            isApellido1Error = false
            apellido1ErrorMessage = ""
        }

        // Validar email
        if (email.isBlank()) {
            isEmailError = true
            emailErrorMessage = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailError = true
            emailErrorMessage = "Email inválido"
            isValid = false
        } else {
            isEmailError = false
            emailErrorMessage = ""
        }

        // Validar contraseña
        if (password.isBlank()) {
            isPasswordError = true
            passwordErrorMessage = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            isPasswordError = true
            passwordErrorMessage = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            isPasswordError = false
            passwordErrorMessage = ""
        }

        // Validar confirmación de contraseña
        if (confirmPassword != password) {
            isConfirmPasswordError = true
            confirmPasswordErrorMessage = "Las contraseñas no coinciden"
            isValid = false
        } else {
            isConfirmPasswordError = false
            confirmPasswordErrorMessage = ""
        }

        // Validar campos específicos según el rol
        when (role.lowercase()) {
            "organizador" -> isValid = isValid && validateOrganizadorFields()
            "participante" -> isValid = isValid && validateParticipanteFields()
        }

        return isValid
    }

    fun validateOrganizadorFields(): Boolean {
        var isValid = true
        
        if (nombreOrganizacion.isBlank()) {
            isNombreOrganizacionError = true
            nombreOrganizacionErrorMessage = "El nombre de la organización es requerido"
            isValid = false
        } else {
            isNombreOrganizacionError = false
            nombreOrganizacionErrorMessage = ""
        }

        if (telefonoContacto.isBlank()) {
            isTelefonoContactoError = true
            telefonoContactoErrorMessage = "El teléfono de contacto es requerido"
            isValid = false
        } else if (!telefonoContacto.matches(Regex("^[0-9]{9}$"))) {
            isTelefonoContactoError = true
            telefonoContactoErrorMessage = "El teléfono debe tener 9 dígitos"
            isValid = false
        } else {
            isTelefonoContactoError = false
            telefonoContactoErrorMessage = ""
        }

        return isValid
    }

    fun validateParticipanteFields(): Boolean {
        var isValid = true

        if (dni.isBlank()) {
            isDniError = true
            dniErrorMessage = "El DNI es requerido"
            isValid = false
        } else if (!dni.matches(Regex("^[0-9]{8}[A-Z]$"))) {
            isDniError = true
            dniErrorMessage = "DNI inválido (8 números y 1 letra)"
            isValid = false
        } else {
            isDniError = false
            dniErrorMessage = ""
        }

        if (telefono.isBlank()) {
            isTelefonoError = true
            telefonoErrorMessage = "El teléfono es requerido"
            isValid = false
        } else if (!telefono.matches(Regex("^[0-9]{9}$"))) {
            isTelefonoError = true
            telefonoErrorMessage = "El teléfono debe tener 9 dígitos"
            isValid = false
        } else {
            isTelefonoError = false
            telefonoErrorMessage = ""
        }

        return isValid
    }

    // Función para limpiar error
    fun clearError() {
        isError = false
        _errorMessage.value = null
    }

    // Función para establecer error
    fun setError(message: String) {
        isError = true
        _errorMessage.value = message
    }

    private fun clearFields() {
        name = ""
        apellido1 = ""
        apellido2 = ""
        email = ""
        password = ""
        confirmPassword = ""
        nombreOrganizacion = ""
        telefonoContacto = ""
        dni = ""
        telefono = ""
        
        // Limpiar estados de error
        isNameError = false
        isApellido1Error = false
        isApellido2Error = false
        isEmailError = false
        isPasswordError = false
        isConfirmPasswordError = false
        isNombreOrganizacionError = false
        isTelefonoContactoError = false
        isDniError = false
        isTelefonoError = false
        
        // Limpiar mensajes de error
        nameErrorMessage = ""
        apellido1ErrorMessage = ""
        apellido2ErrorMessage = ""
        emailErrorMessage = ""
        passwordErrorMessage = ""
        confirmPasswordErrorMessage = ""
        nombreOrganizacionErrorMessage = ""
        telefonoContactoErrorMessage = ""
        dniErrorMessage = ""
        telefonoErrorMessage = ""
    }

    private fun mostrarMensaje(mensaje: String) {
        _debugMessage.value = mensaje
        Log.d("REGISTRO_DEBUG", mensaje)
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            try {
                mostrarMensaje("Iniciando registro de ${role.lowercase()}...")
                
                isLoading = true
                clearError()
                
                if (!validateAllFields()) {
                    setError("Por favor, corrige los errores en el formulario")
                    return@launch
                }

                when (role.lowercase()) {
                    "organizador" -> {
                        if (!validateOrganizadorFields()) {
                            setError("Por favor, completa los datos del organizador correctamente")
                            return@launch
                        }
                    }
                    "participante" -> {
                        if (!validateParticipanteFields()) {
                            setError("Por favor, completa los datos del participante correctamente")
                            return@launch
                        }
                    }
                }

                val registerRequest = RegisterRequest(
                    nombre = name,
                    apellido1 = apellido1,
                    apellido2 = apellido2,
                    email = email,
                    password = password,
                    role = role.lowercase(),
                    nombreOrganizacion = if (role.lowercase() == "organizador") nombreOrganizacion else null,
                    telefonoContacto = if (role.lowercase() == "organizador") telefonoContacto else null,
                    dni = if (role.lowercase() == "participante") dni else null,
                    telefono = if (role.lowercase() == "participante") telefono else null
                )

                mostrarMensaje("Enviando datos: $registerRequest")
                
                val response = RetrofitClient.apiService.registerUser(registerRequest)
                
                if (response.isSuccessful) {
                    mostrarMensaje("Registro exitoso: ${response.body()}")
                    _isRegisterSuccessful.value = true
                    clearFields()
                } else {
                    val errorBody = response.errorBody()?.string()
                    mostrarMensaje("Error en el registro: $errorBody")
                    setError("Error en el registro: ${response.message()}")
                }
            } catch (e: Exception) {
                mostrarMensaje("Error: ${e.message}")
                setError("Error de conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun getPasswordRequirements(): List<String> {
        return listOf(
            "Al menos 8 caracteres",
            "Al menos una letra mayúscula",
            "Al menos una letra minúscula",
            "Al menos un número",
            "Al menos un carácter especial (@#$%^&+=)"
        )
    }

    // Función para resetear el estado de registro exitoso
    fun resetSuccessState() {
        _isRegisterSuccessful.value = false
    }

    fun registerParticipante() {
        viewModelScope.launch {
            try {
                if (!validateParticipanteFields()) return@launch
                
                isLoading = true
                clearError()

                val registerRequest = RegisterRequest(
                    nombre = name,
                    apellido1 = apellido1,
                    apellido2 = apellido2,
                    email = email,
                    password = password,
                    role = "participante",
                    dni = dni,
                    telefono = telefono,
                    nombreOrganizacion = null,
                    telefonoContacto = null
                )

                val response = RetrofitClient.apiService.registerUser(registerRequest)

                if (response.isSuccessful) {
                    _isRegisterSuccessful.value = true
                    clearFields()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Error en el registro"
                    } catch (e: Exception) {
                        "Error en el registro"
                    }
                    setError(errorMessage)
                }
            } catch (e: Exception) {
                setError("Error de conexión: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }
} 