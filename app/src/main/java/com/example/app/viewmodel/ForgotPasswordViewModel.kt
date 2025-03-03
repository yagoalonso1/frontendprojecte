package com.example.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.RetrofitClient
import com.example.app.model.resetpassword.ResetPasswordRequest
import com.example.app.model.resetpassword.ResetPasswordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordViewModel : ViewModel() {
    // Estado para el email e identificador
    var email by mutableStateOf("")
        private set
    var identificador by mutableStateOf("")
        private set
    
    // Estado para la contraseña recuperada
    var recoveredPassword by mutableStateOf("")
        private set
    
    // Estado para mostrar errores
    private val _debugMessage = MutableLiveData<String>()
    val debugMessage: LiveData<String> = _debugMessage
    
    // Estado para errores
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    // Estado para navegación
    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin
    
    // Estado para indicar carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Función para actualizar el email
    fun onEmailChange(newEmail: String) {
        email = newEmail
    }
    
    // Función para actualizar el identificador
    fun onIdentificadorChange(newIdentificador: String) {
        identificador = newIdentificador
    }
    
    // Función para resetear la contraseña
    fun onResetPasswordClick() {
        viewModelScope.launch {
            try {
                // Validar email
                if (!isValidEmail(email)) {
                    setError("Por favor, introduce un email válido")
                    return@launch
                }
                
                // Validar identificador
                if (identificador.isBlank()) {
                    setError("Por favor, introduce tu DNI o teléfono")
                    return@launch
                }
                
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("RESET_PASSWORD", "Enviando solicitud de reset para: $email con identificador: $identificador")
                
                // Crear objeto de petición
                val resetRequest = ResetPasswordRequest(email = email, identificador = identificador)
                
                // Imprimir la solicitud para depuración
                Log.d("RESET_PASSWORD", "Request: $resetRequest")
                
                try {
                    // Llamar a la API
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.resetPassword(resetRequest)
                    }
                    
                    // Imprimir la respuesta para depuración
                    Log.d("RESET_PASSWORD", "Response code: ${response.code()}")
                    Log.d("RESET_PASSWORD", "Response message: ${response.message()}")
                    Log.d("RESET_PASSWORD", "Response body: ${response.body()}")
                    
                    if (response.isSuccessful) {
                        Log.d("RESET_PASSWORD", "Solicitud enviada correctamente")
                        
                        // Extraer la contraseña de la respuesta
                        val responseBody = response.body()
                        if (responseBody != null && responseBody.password != null && responseBody.password.isNotEmpty()) {
                            recoveredPassword = responseBody.password
                            _debugMessage.value = "Contraseña recuperada con éxito"
                        } else {
                            setError("No se pudo recuperar la contraseña: Respuesta vacía")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("RESET_PASSWORD", "Error: ${response.code()} - $errorBody")
                        
                        // Intentar extraer el mensaje de error
                        try {
                            val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                            val message = errorResponse?.get("message") as? String ?: errorResponse?.get("error") as? String
                            setError(message ?: "No se pudo recuperar la contraseña (${response.code()})")
                        } catch (e: Exception) {
                            setError("No se pudo recuperar la contraseña: ${response.message()} (${response.code()})")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RESET_PASSWORD", "Error en la llamada a la API: ${e.message}", e)
                    setError("Error en la llamada a la API: ${e.message}")
                }
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Log.e("RESET_PASSWORD", "Excepción general: ${e.message}", e)
                setError("Error de conexión: ${e.message}")
            }
        }
    }
    
    // Función para validar email
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Función para establecer error
    fun setError(error: String?) {
        isError = error != null
        errorMessage = error
        Log.e("RESET_PASSWORD", "ERROR: $error")
    }
    
    // Función para resetear la navegación
    fun onLoginNavigated() {
        _navigateToLogin.value = false
    }
    
    // Función para navegar al login
    fun navigateToLogin() {
        _navigateToLogin.value = true
    }
}

// Clase para la petición de reset
data class ResetPasswordRequest(
    val email: String,
    val identificador: String
)

// Clase para la respuesta de reset
data class ResetPasswordResponse(
    val password: String?
)