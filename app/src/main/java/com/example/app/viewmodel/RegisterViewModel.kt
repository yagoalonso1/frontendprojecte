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
            mostrarMensaje("ERROR: Nombre vacío")
        } else {
            isNameError = false
            nameErrorMessage = ""
        }

        if (apellido1.isBlank()) {
            isApellido1Error = true
            apellido1ErrorMessage = "El primer apellido es requerido"
            isValid = false
            mostrarMensaje("ERROR: Apellido1 vacío")
        } else {
            isApellido1Error = false
            apellido1ErrorMessage = ""
        }

        // Validar email
        if (email.isBlank()) {
            isEmailError = true
            emailErrorMessage = "El email es requerido"
            isValid = false
            mostrarMensaje("ERROR: Email vacío")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailError = true
            emailErrorMessage = "Email inválido"
            isValid = false
            mostrarMensaje("ERROR: Email inválido: $email")
        } else {
            isEmailError = false
            emailErrorMessage = ""
        }

        // Validar contraseña
        if (password.isBlank()) {
            isPasswordError = true
            passwordErrorMessage = "La contraseña es requerida"
            isValid = false
            mostrarMensaje("ERROR: Contraseña vacía")
        } else if (password.length < 6) {
            isPasswordError = true
            passwordErrorMessage = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
            mostrarMensaje("ERROR: Contraseña demasiado corta: ${password.length} caracteres")
        } else {
            isPasswordError = false
            passwordErrorMessage = ""
        }

        // Validar confirmación de contraseña
        if (confirmPassword != password) {
            isConfirmPasswordError = true
            confirmPasswordErrorMessage = "Las contraseñas no coinciden"
            isValid = false
            mostrarMensaje("ERROR: Las contraseñas no coinciden")
        } else {
            isConfirmPasswordError = false
            confirmPasswordErrorMessage = ""
        }

        // Validar campos específicos según el rol
        when (role.lowercase()) {
            "organizador" -> {
                val validOrg = validateOrganizadorFields()
                if (!validOrg) {
                    mostrarMensaje("ERROR: Validación de campos de organizador falló")
                }
                isValid = isValid && validOrg
            }
            "participante" -> {
                val validPart = validateParticipanteFields()
                if (!validPart) {
                    mostrarMensaje("ERROR: Validación de campos de participante falló")
                }
                isValid = isValid && validPart
            }
        }

        mostrarMensaje("Resultado final de validación: ${if (isValid) "VÁLIDO" else "INVÁLIDO"}")
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
            mostrarMensaje("ERROR: DNI vacío")
        } else if (!dni.matches(Regex("^[0-9]{8}[A-Z]$"))) {
            isDniError = true
            dniErrorMessage = "DNI inválido (8 números y 1 letra)"
            isValid = false
            mostrarMensaje("ERROR: Formato de DNI inválido: $dni")
        } else {
            // Validar la letra del DNI
            val letters = "TRWAGMYFPDXBNJZSQVHLCKE"
            val number = dni.substring(0, 8).toInt()
            val letter = dni.last()
            if (letters[number % 23] != letter) {
                isDniError = true
                dniErrorMessage = "La letra del DNI no es correcta"
                isValid = false
                mostrarMensaje("ERROR: Letra de DNI incorrecta. Calculada: ${letters[number % 23]}, Proporcionada: $letter")
            } else {
                isDniError = false
                dniErrorMessage = ""
            }
        }

        if (telefono.isBlank()) {
            isTelefonoError = true
            telefonoErrorMessage = "El teléfono es requerido"
            isValid = false
            mostrarMensaje("ERROR: Teléfono vacío")
        } else if (!telefono.matches(Regex("^[0-9]{9}$"))) {
            isTelefonoError = true
            telefonoErrorMessage = "El teléfono debe tener 9 dígitos"
            isValid = false
            mostrarMensaje("ERROR: Formato de teléfono inválido: $telefono (longitud: ${telefono.length})")
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

    fun mostrarMensaje(mensaje: String) {
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

                // Asegurarse de que el rol siempre esté en minúsculas
                role = role.lowercase()
                
                // Crear objeto de solicitud según el rol
                val registerRequest = when (role) {
                    "participante" -> {
                        mostrarMensaje("Creando solicitud para participante")
                        RegisterRequest.createParticipante(
                            nombre = name,
                            apellido1 = apellido1,
                            apellido2 = apellido2 ?: "",
                            email = email,
                            password = password,
                            dni = dni,
                            telefono = telefono
                        )
                    }
                    "organizador" -> {
                        mostrarMensaje("Creando solicitud para organizador")
                        RegisterRequest.createOrganizador(
                            nombre = name,
                            apellido1 = apellido1,
                            apellido2 = apellido2 ?: "",
                            email = email,
                            password = password,
                            nombreOrganizacion = nombreOrganizacion,
                            telefonoContacto = telefonoContacto
                        )
                    }
                    else -> {
                        mostrarMensaje("Rol no reconocido: ${role}")
                        setError("Rol no válido")
                        isLoading = false
                        return@launch
                    }
                }

                // Mostrar los valores exactos que se están enviando
                mostrarMensaje("Enviando solicitud de registro con role: ${registerRequest.role}")
                mostrarMensaje("nombreOrganizacion: '${registerRequest.nombreOrganizacion}' (longitud: ${registerRequest.nombreOrganizacion.length})")
                mostrarMensaje("telefonoContacto: '${registerRequest.telefonoContacto}' (longitud: ${registerRequest.telefonoContacto.length})")
                mostrarMensaje("JSON completo: ${Gson().toJson(registerRequest)}")
                
                try {
                    val response = RetrofitClient.apiService.registerUser(registerRequest)
                    
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        mostrarMensaje("Registro exitoso: ${registerResponse}")
                        
                        // Guardar token y datos de usuario en SessionManager
                        if (registerResponse != null) {
                            // Determinar qué token usar (puede ser token o accessToken)
                            val tokenToSave = when {
                                registerResponse.token != null -> {
                                    mostrarMensaje("Usando 'token' de la respuesta")
                                    registerResponse.token
                                }
                                registerResponse.accessToken != null -> {
                                    mostrarMensaje("Usando 'access_token' de la respuesta")
                                    registerResponse.accessToken
                                }
                                else -> {
                                    mostrarMensaje("No se encontró token en la respuesta, intentando iniciar sesión")
                                    // Si no hay token, intentamos hacer login automáticamente
                                    loginAfterRegistration()
                                    null
                                }
                            }
                            
                            // Si tenemos un token, guardarlo
                            if (tokenToSave != null) {
                                mostrarMensaje("Guardando token: ${tokenToSave.take(10)}...")
                                com.example.app.util.SessionManager.saveToken(tokenToSave)
                                
                                // Guardar rol de usuario
                                mostrarMensaje("Guardando rol: $role")
                                com.example.app.util.SessionManager.saveUserRole(role.lowercase())
                            }
                            
                            // Guardar user_id si está disponible
                            if (registerResponse.user != null) {
                                mostrarMensaje("Usuario registrado con ID: ${registerResponse.user.id}")
                            }
                        } else {
                            mostrarMensaje("Respuesta vacía del servidor")
                        }
                        
                        _isRegisterSuccessful.value = true
                        clearFields()
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        mostrarMensaje("Error en el registro: Código $errorCode - Body: $errorBody")
                        
                        when (errorCode) {
                            422 -> {
                                // Unprocessable Content - Validación fallida
                                try {
                                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                    mostrarMensaje("Error response parseada: $errorResponse")
                                    mostrarMensaje("Mensajes de validación: ${errorResponse.messages}")
                                    
                                    val mensajeError = when {
                                        !errorResponse.messages.isNullOrEmpty() -> {
                                            val errores = mutableListOf<String>()
                                            errorResponse.messages.forEach { (campo, msgs) ->
                                                errores.add("${campo}: ${msgs.joinToString()}")
                                            }
                                            errores.joinToString("\n")
                                        }
                                        errorResponse.message != null -> errorResponse.message
                                        errorResponse.error != null -> errorResponse.error
                                        else -> "Error de validación. Revisa tus datos."
                                    }
                                    
                                    setError(mensajeError)
                                    mostrarMensaje("Error de validación desglosado: $mensajeError")
                                } catch (e: Exception) {
                                    setError("Error de validación: ${e.message}")
                                    mostrarMensaje("Error al procesar respuesta de error: ${e.message}")
                                    mostrarMensaje("Error body original: $errorBody")
                                }
                            }
                            409 -> setError("El correo electrónico ya está registrado")
                            500 -> setError("Error en el servidor. Inténtalo más tarde")
                            else -> setError("Error en el registro: ${response.message()}")
                        }
                    }
                } catch (e: Exception) {
                    mostrarMensaje("Excepción al hacer la petición: ${e.message}")
                    setError("Error de conexión: ${e.message}")
                } finally {
                    isLoading = false
                }
            } catch (e: Exception) {
                mostrarMensaje("Error general: ${e.message}")
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
                // Verificar que los campos básicos estén presentes
                if (name.isBlank() || apellido1.isBlank() || email.isBlank() || password.isBlank()) {
                    mostrarMensaje("ERROR: Faltan campos básicos para el registro")
                    mostrarMensaje("Nombre: $name, Apellido1: $apellido1, Email: $email, Password: ${if (password.isBlank()) "vacío" else "tiene ${password.length} caracteres"}")
                    setError("Faltan datos básicos. Asegúrate de completar toda la información.")
                    return@launch
                }
                
                if (!validateParticipanteFields()) {
                    mostrarMensaje("ERROR: La validación de campos de participante falló")
                    return@launch
                }
                
                isLoading = true
                clearError()

                // Asegurar que el rol está en minúsculas
                role = "participante"
                
                val registerRequest = RegisterRequest.createParticipante(
                    nombre = name,
                    apellido1 = apellido1,
                    apellido2 = apellido2 ?: "",
                    email = email,
                    password = password,
                    dni = dni,
                    telefono = telefono
                )

                // Convertir la solicitud a JSON para verificar exactamente qué se está enviando
                val requestJson = Gson().toJson(registerRequest)
                mostrarMensaje("JSON ENVIADO AL SERVIDOR: $requestJson")
                
                // Mostrar los valores exactos que se están enviando
                mostrarMensaje("Enviando solicitud de registro con role: ${registerRequest.role}")
                mostrarMensaje("nombreOrganizacion: '${registerRequest.nombreOrganizacion}' (longitud: ${registerRequest.nombreOrganizacion.length}, tipo: ${registerRequest.nombreOrganizacion::class.java.simpleName})")
                mostrarMensaje("telefonoContacto: '${registerRequest.telefonoContacto}' (longitud: ${registerRequest.telefonoContacto.length}, tipo: ${registerRequest.telefonoContacto::class.java.simpleName})")

                val response = RetrofitClient.apiService.registerUser(registerRequest)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    mostrarMensaje("Registro exitoso: ${registerResponse}")
                    
                    // Guardar token y datos de usuario en SessionManager
                    if (registerResponse != null) {
                        // Determinar qué token usar (puede ser token o accessToken)
                        val tokenToSave = when {
                            registerResponse.token != null -> {
                                mostrarMensaje("Usando 'token' de la respuesta")
                                registerResponse.token
                            }
                            registerResponse.accessToken != null -> {
                                mostrarMensaje("Usando 'access_token' de la respuesta")
                                registerResponse.accessToken
                            }
                            else -> {
                                mostrarMensaje("No se encontró token en la respuesta, intentando iniciar sesión")
                                // Si no hay token, intentamos hacer login automáticamente
                                loginAfterRegistration()
                                null
                            }
                        }
                        
                        // Si tenemos un token, guardarlo
                        if (tokenToSave != null) {
                            mostrarMensaje("Guardando token: ${tokenToSave.take(10)}...")
                            com.example.app.util.SessionManager.saveToken(tokenToSave)
                            
                            // Guardar rol de usuario
                            mostrarMensaje("Guardando rol: $role")
                            com.example.app.util.SessionManager.saveUserRole(role.lowercase())
                        }
                        
                        // Guardar user_id si está disponible
                        if (registerResponse.user != null) {
                            mostrarMensaje("Usuario registrado con ID: ${registerResponse.user.id}")
                        }
                    } else {
                        mostrarMensaje("Respuesta vacía del servidor")
                    }
                    
                    _isRegisterSuccessful.value = true
                    clearFields()
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    mostrarMensaje("ERROR EN EL REGISTRO: Código $errorCode")
                    mostrarMensaje("CUERPO DE RESPUESTA COMPLETO: $errorBody")
                    
                    when (errorCode) {
                        422 -> {
                            // Unprocessable Content - Validación fallida
                            try {
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                mostrarMensaje("Error response parseada: $errorResponse")
                                
                                val mensajeError = when {
                                    !errorResponse.messages.isNullOrEmpty() -> {
                                        val errores = mutableListOf<String>()
                                        errorResponse.messages.forEach { (campo, msgs) ->
                                            errores.add("${campo}: ${msgs.joinToString()}")
                                        }
                                        errores.joinToString("\n")
                                    }
                                    errorResponse.message != null -> errorResponse.message
                                    errorResponse.error != null -> errorResponse.error
                                    else -> "Error de validación. Revisa tus datos."
                                }
                                
                                setError(mensajeError)
                            } catch (e: Exception) {
                                setError("Error de validación: ${e.message ?: "Error desconocido"}")
                                mostrarMensaje("Error al procesar respuesta de error: ${e.message}")
                                mostrarMensaje("Error body original: $errorBody")
                            }
                        }
                        409 -> setError("El correo electrónico ya está registrado")
                        400 -> setError("Datos de registro incorrectos. Revisa la información proporcionada.")
                        500 -> setError("Error en el servidor. Inténtalo más tarde")
                        else -> setError("Error en el registro (código $errorCode): ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                mostrarMensaje("Error de conexión en registerParticipante: ${e.message}")
                setError("Error de conexión: ${e.localizedMessage ?: e.message ?: "Error desconocido"}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun loginAfterRegistration() {
        viewModelScope.launch {
            try {
                mostrarMensaje("Intentando login automático después del registro")
                isLoading = true
                
                // Crear objeto de login con las credenciales del registro
                val loginRequest = com.example.app.model.login.LoginRequest(
                    email = email,
                    password = password
                )
                
                // Realizar petición de login
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.app.api.RetrofitClient.apiService.loginUser(loginRequest)
                }
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    mostrarMensaje("Login automático exitoso: $loginResponse")
                    
                    if (loginResponse != null) {
                        // Obtener token
                        val token = loginResponse.accessToken ?: loginResponse.token
                        
                        if (token != null) {
                            mostrarMensaje("Guardando token del login: ${token.take(10)}...")
                            com.example.app.util.SessionManager.saveToken(token)
                            
                            // Guardar rol
                            val userRole = role.lowercase()
                            mostrarMensaje("Guardando rol del login: $userRole")
                            com.example.app.util.SessionManager.saveUserRole(userRole)
                        } else {
                            mostrarMensaje("No se encontró token en la respuesta de login")
                        }
                    }
                } else {
                    mostrarMensaje("Error en login automático: ${response.code()}")
                }
            } catch (e: Exception) {
                mostrarMensaje("Error al realizar login automático: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
} 