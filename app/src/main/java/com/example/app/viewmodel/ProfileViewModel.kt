package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.ProfileData
import com.example.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    // Estado del perfil
    var profileData by mutableStateOf<ProfileData?>(null)
        private set
    
    // Estado de edición
    var isEditing by mutableStateOf(false)
        private set
    
    // Campos editables
    var nombre by mutableStateOf("")
    var apellido1 by mutableStateOf("")
    var apellido2 by mutableStateOf("")
    var email by mutableStateOf("")
    
    // Campos específicos para participante
    var dni by mutableStateOf("")
    var telefono by mutableStateOf("")
    
    // Campos específicos para organizador
    var nombreOrganizacion by mutableStateOf("")
    var telefonoContacto by mutableStateOf("")
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Estado de actualización
    private val _isUpdateSuccessful = MutableStateFlow(false)
    val isUpdateSuccessful = _isUpdateSuccessful.asStateFlow()
    
    // Inicializar cargando el perfil
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                // Obtener token
                val token = SessionManager.getToken()
                Log.d("ProfileViewModel", "Token obtenido del SessionManager: $token")
                
                if (token.isNullOrEmpty()) {
                    Log.e("ProfileViewModel", "Error: No hay token disponible")
                    setError("No se ha iniciado sesión")
                    return@launch
                }
                
                Log.d("ProfileViewModel", "Intentando obtener información del usuario primero con /api/user")
                
                // Intentar primero con el endpoint /api/user
                try {
                    val userResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.getUser("Bearer $token")
                    }
                    
                    if (userResponse.isSuccessful) {
                        val user = userResponse.body()
                        Log.d("ProfileViewModel", "Respuesta exitosa de /api/user: $user")
                        
                        if (user != null) {
                            // Crear un ProfileData a partir del User
                            profileData = ProfileData(
                                id = user.id,
                                nombre = user.nombre,
                                apellido1 = user.apellido1,
                                apellido2 = user.apellido2,
                                email = user.email,
                                role = user.role,
                                // Los demás campos quedarán como null inicialmente
                            )
                            
                            // Inicializar campos editables
                            initEditableFields()
                            
                            // Si conseguimos la información básica, intentamos obtener los detalles completos
                            Log.d("ProfileViewModel", "Obteniendo detalles adicionales del perfil...")
                            loadProfileDetails(token)
                            return@launch
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error al obtener usuario: ${userResponse.code()}")
                        // No hacemos return aquí para que intente con el siguiente endpoint
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error al llamar a /api/user: ${e.message}", e)
                    // Continuamos con el siguiente endpoint
                }
                
                // Si llegamos aquí, el primer intento falló, intentamos con /api/profile
                Log.d("ProfileViewModel", "Intentando con endpoint alternativo /api/profile")
                loadProfileFromProfileEndpoint(token)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error general al cargar perfil: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun loadProfileFromProfileEndpoint(token: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Realizando petición a getProfile con token: Bearer ${token.take(10)}...")
                
                // Realizar la petición
                val response = withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.apiService.getProfile("Bearer $token")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error al realizar petición HTTP a /api/profile: ${e.message}", e)
                        return@withContext null
                    }
                }
                
                // Procesar la respuesta
                withContext(Dispatchers.Main) {
                    if (response == null) {
                        setError("Error de conexión al servidor")
                        return@withContext
                    }
                    
                    if (response.isSuccessful) {
                        val profileResponse = response.body()
                        Log.d("ProfileViewModel", "Respuesta exitosa de /api/profile: ${profileResponse?.message}")
                        
                        if (profileResponse != null && profileResponse.data != null) {
                            profileData = profileResponse.data
                            Log.d("ProfileViewModel", "Datos de perfil recibidos: $profileData")
                            // Inicializar campos editables
                            initEditableFields()
                        } else {
                            Log.e("ProfileViewModel", "Cuerpo de respuesta vacío o sin datos")
                            setError("No se pudo obtener la información del perfil")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ProfileViewModel", "Error ${response.code()}: $errorBody")
                        
                        when (response.code()) {
                            401 -> setError("Tu sesión ha expirado. Por favor, inicia sesión nuevamente")
                            403 -> setError("No tienes permiso para acceder a esta información")
                            404 -> setError("Perfil no encontrado")
                            500 -> setError("Error interno del servidor")
                            else -> setError("Error al cargar el perfil: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error en loadProfileFromProfileEndpoint: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            }
        }
    }
    
    private fun loadProfileDetails(token: String) {
        // Este método se puede implementar para obtener información adicional
        // específica del tipo de usuario (participante u organizador)
        // Por ahora no hacemos nada aquí, pero podría implementarse en el futuro
    }
    
    private fun initEditableFields() {
        profileData?.let { profile ->
            nombre = profile.nombre ?: ""
            apellido1 = profile.apellido1 ?: ""
            apellido2 = profile.apellido2 ?: ""
            email = profile.email ?: ""
            
            // Campos por rol
            if (profile.role == "participante") {
                dni = profile.dni ?: ""
                telefono = profile.telefono ?: ""
            } else if (profile.role == "organizador") {
                nombreOrganizacion = profile.nombreOrganizacion ?: ""
                telefonoContacto = profile.telefonoContacto ?: ""
            }
        }
    }
    
    fun startEditing() {
        isEditing = true
    }
    
    fun cancelEditing() {
        isEditing = false
        initEditableFields() // Restaurar campos originales
        clearError()
    }
    
    fun saveProfile() {
        viewModelScope.launch {
            try {
                isLoading = true
                clearError()
                
                // Obtener token
                val token = SessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    setError("No se ha iniciado sesión")
                    return@launch
                }
                
                // Crear mapa con los datos a actualizar usando valores String explícitos
                val updateData = HashMap<String, String>()
                
                profileData?.let { profile ->
                    // Datos básicos
                    updateData["nombre"] = nombre
                    updateData["apellido1"] = apellido1
                    if (apellido2.isNotEmpty()) {
                        updateData["apellido2"] = apellido2
                    } else {
                        updateData["apellido2"] = "" // Asegurar que se envía vacío y no null
                    }
                    
                    // Añadir todos los campos específicos para cada rol,
                    // independientemente del rol del usuario actual
                    // Campos de participante
                    updateData["dni"] = dni
                    updateData["telefono"] = telefono
                    
                    // Campos de organizador
                    updateData["nombre_organizacion"] = nombreOrganizacion
                    updateData["telefono_contacto"] = telefonoContacto
                }
                
                Log.d("ProfileViewModel", "Enviando datos: $updateData")
                
                try {
                    // Realizar la petición de actualización
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.updateProfile(
                            "Bearer $token",
                            updateData
                        )
                    }
                    
                    // Procesar la respuesta
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val profileResponse = response.body()
                            if (profileResponse != null && profileResponse.data != null) {
                                profileData = profileResponse.data
                                isEditing = false
                                _isUpdateSuccessful.value = true
                            } else {
                                setError("No se pudo actualizar la información del perfil")
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: ""
                            Log.e("ProfileViewModel", "Error raw: $errorBody")
                            
                            // Verificar si la respuesta es HTML (error del servidor)
                            if (errorBody.trim().startsWith("<!DOCTYPE html>") || 
                                errorBody.trim().startsWith("<html")) {
                                Log.e("ProfileViewModel", "Error del servidor (respuesta HTML)")
                                setError("Error en el servidor. Por favor, intente más tarde")
                            } else {
                                // Intentar analizar como JSON primero
                                try {
                                    // Si hay un mensaje específico en el errorBody, lo extraemos
                                    val errorMessage = if (errorBody.contains("\"message\"")) {
                                        // Extraer el mensaje del JSON usando una expresión regular simple
                                        val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                        val matchResult = regex.find(errorBody)
                                        matchResult?.groupValues?.get(1) ?: "Error desconocido"
                                    } else {
                                        "Error: ${response.code()}"
                                    }
                                    setError(errorMessage)
                                } catch (e: Exception) {
                                    // Si no es JSON, mostramos el error tal cual
                                    setError("Error al actualizar el perfil: ${response.code()}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error en la petición HTTP: ${e.message}")
                    if (e.message?.contains("Unable to resolve host") == true) {
                        setError("No hay conexión a Internet. Verifica tu conexión e intenta nuevamente.")
                    } else {
                        setError("Error de conexión: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error general: ${e.message}")
                setError("Error inesperado: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetUpdateState() {
        _isUpdateSuccessful.value = false
    }
    
    private fun setError(message: String) {
        errorMessage = message
    }
    
    private fun clearError() {
        errorMessage = null
    }
} 