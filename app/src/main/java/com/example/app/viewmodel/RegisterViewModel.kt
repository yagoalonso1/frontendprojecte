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

class RegisterViewModel : ViewModel() {
    // Estados del formulario principal
    var name by mutableStateOf("")
    var apellido1 by mutableStateOf("")
    var apellido2 by mutableStateOf("")
    var email by mutableStateOf("")
    var dni by mutableStateOf("")
    var telefono by mutableStateOf("")
    var password by mutableStateOf("")
    var repeatPassword by mutableStateOf("")
    var role by mutableStateOf("")
    
    // Estados de error del formulario principal
    var isNameError by mutableStateOf(false)
    var isApellido1Error by mutableStateOf(false)
    var isApellido2Error by mutableStateOf(false)
    var isEmailError by mutableStateOf(false)
    var isDniError by mutableStateOf(false)
    var isTelefonoError by mutableStateOf(false)
    var isPasswordError by mutableStateOf(false)
    var isRepeatPasswordError by mutableStateOf(false)
    
    // Estados específicos de Organizador
    var nombreOrganizacion by mutableStateOf("")
    var telefonoContacto by mutableStateOf("")
    var isNombreOrganizacionError by mutableStateOf(false)
    var isTelefonoContactoError by mutableStateOf(false)
    
    // Estados específicos de Participante
    // (Ya tenemos dni y telefono en el formulario principal)
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)
    
    // Validaciones de formato
    private val nameRegex = "^[A-Za-zÁ-ÿ\\s]{2,}$"
    private val dniRegex = "^[0-9]{8}[A-Z]$"
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    private val phoneRegex = "^[0-9]{9}$"
    private val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[.@#$%^&+=!*()_\\-])(?=\\S+$).{8,}$"

    // Cambiamos a StateFlow para poder observarlo en el NavHost
    private val _isRegisterSuccessful = MutableStateFlow(false)
    val isRegisterSuccessful = _isRegisterSuccessful.asStateFlow()

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
        return password == repeatPassword && password.isNotEmpty()
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
            "repeatPassword" -> isRepeatPasswordError = !doPasswordsMatch()
            "nombreOrganizacion" -> isNombreOrganizacionError = !isValidNombreOrganizacion(value)
            "telefonoContacto" -> isTelefonoContactoError = !isValidPhone(value)
        }
    }

    fun validateAllFields(): Boolean {
        validateField("name", name)
        validateField("apellido1", apellido1)
        validateField("apellido2", apellido2)
        validateField("email", email)
        validateField("dni", dni)
        validateField("telefono", telefono)
        validateField("password", password)
        validateField("repeatPassword", repeatPassword)
        
        return !isNameError && !isApellido1Error && !isApellido2Error &&
               !isEmailError && !isDniError && !isTelefonoError &&
               !isPasswordError && !isRepeatPasswordError &&
               name.isNotEmpty() && apellido1.isNotEmpty() && 
               email.isNotEmpty() && dni.isNotEmpty() &&
               telefono.isNotEmpty() && password.isNotEmpty() &&
               repeatPassword.isNotEmpty()
    }

    fun validateOrganizadorFields(): Boolean {
        validateField("nombreOrganizacion", nombreOrganizacion)
        validateField("telefonoContacto", telefonoContacto)
        
        return !isNombreOrganizacionError && !isTelefonoContactoError &&
               nombreOrganizacion.isNotEmpty() && telefonoContacto.isNotEmpty()
    }

    fun validateParticipanteFields(): Boolean {
        validateField("dni", dni)
        validateField("telefono", telefono)
        
        return !isDniError && !isTelefonoError &&
               dni.isNotEmpty() && telefono.isNotEmpty()
    }

    fun isAllFieldsValid(): Boolean {
        return validateAllFields()
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                if (!validateAllFields()) {
                    setError("Por favor, corrige los errores en el formulario")
                    return@launch
                }

                // Validar campos específicos según el rol
                when (role.lowercase()) {
                    "organizador" -> {
                        if (!validateOrganizadorFields()) {
                            setError("Por favor, completa los datos del organizador")
                            return@launch
                        }
                    }
                    "participante" -> {
                        if (!validateParticipanteFields()) {
                            setError("Por favor, completa los datos del participante")
                            return@launch
                        }
                    }
                }

                // Crear objeto de petición
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

                // Añadir logs para depuración
                Log.d("RegisterViewModel", "Enviando petición: $registerRequest")

                // Llamar a la API
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.registerUser(registerRequest)
                }

                Log.d("RegisterViewModel", "Código de respuesta: ${response.code()}")
                Log.d("RegisterViewModel", "Cuerpo de respuesta: ${response.body()}")
                
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        _isRegisterSuccessful.value = true
                        clearFields()
                    } else {
                        setError("Error desconocido durante el registro")
                    }
                } else {
                    // Manejar errores específicos según el código de respuesta
                    when (response.code()) {
                        409 -> {
                            // Conflicto - recurso ya existe
                            if (role.lowercase() == "participante") {
                                setError("Ya existe un participante con este DNI o correo electrónico")
                            } else {
                                setError("Ya existe un organizador con este correo electrónico")
                            }
                        }
                        422 -> {
                            // Error de validación
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.d("RegisterViewModel", "Error body: $errorBody")
                                
                                // Intentar extraer mensajes de error específicos
                                val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                val errors = errorResponse["errors"] as? Map<*, *>
                                
                                if (errors != null) {
                                    // Construir un mensaje de error más amigable
                                    val errorMessages = mutableListOf<String>()
                                    
                                    if (errors.containsKey("email")) {
                                        errorMessages.add("El correo electrónico ya está en uso")
                                    }
                                    
                                    if (errors.containsKey("dni")) {
                                        errorMessages.add("El DNI ya está registrado")
                                    }
                                    
                                    if (errorMessages.isNotEmpty()) {
                                        setError(errorMessages.joinToString("\n"))
                                    } else {
                                        // Mensaje genérico si no podemos identificar el error específico
                                        val message = errorResponse["message"] as? String
                                        setError(message ?: "Error de validación en los datos enviados")
                                    }
                                } else {
                                    val message = errorResponse["message"] as? String
                                    setError(message ?: "Error de validación en los datos enviados")
                                }
                            } catch (e: Exception) {
                                Log.e("RegisterViewModel", "Error al procesar respuesta de error", e)
                                setError("Error de validación: Verifica que los datos sean correctos")
                            }
                        }
                        401, 403 -> setError("No tienes permiso para realizar esta acción")
                        404 -> setError("El recurso solicitado no existe")
                        500 -> setError("Error interno del servidor. Inténtalo más tarde")
                        else -> {
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.e("RegisterViewModel", "Error body: $errorBody")
                                val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                val message = errorResponse["message"] as? String
                                setError(message ?: "Error en la comunicación con el servidor: ${response.code()}")
                            } catch (e: Exception) {
                                setError("Error en la comunicación con el servidor: ${response.code()}")
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error durante el registro", e)
                setError("Error durante el registro: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun clearFields() {
        name = ""
        apellido1 = ""
        apellido2 = ""
        email = ""
        dni = ""
        telefono = ""
        password = ""
        repeatPassword = ""
        nombreOrganizacion = ""
        telefonoContacto = ""
        role = ""
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

    fun clearError() {
        isError = false
        errorMessage = null
    }

    fun setError(message: String) {
        isError = true
        errorMessage = message
    }

    // Función para resetear el estado de registro exitoso
    fun resetSuccessState() {
        _isRegisterSuccessful.value = false
    }
} 