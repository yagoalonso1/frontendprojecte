package com.example.app.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.api.ApiService
import com.example.app.api.RetrofitClient
import com.example.app.model.Evento
import com.example.app.model.evento.EventoResponse
import com.example.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventoViewModel : ViewModel() {
    // Estado para la lista de eventos
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    
    // Estado para la lista de eventos del organizador
    var misEventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    
    // Estado para indicar carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Estado para errores
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    
    private val _isRegisterSuccessful = MutableStateFlow(false)
    val isRegisterSuccessful: StateFlow<Boolean> = _isRegisterSuccessful
    
    // Cargar eventos al iniciar
    init {
        fetchEventos()
    }
    
    // Función para cargar eventos
    fun fetchEventos() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("EVENTOS", "Cargando eventos...")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllEventos()
                }
                
                if (response.isSuccessful) {
                    val eventosResponse = response.body()
                    if (eventosResponse != null) {
                        eventos = eventosResponse.eventos
                        Log.d("EVENTOS", "Eventos cargados: ${eventos.size}")
                    } else {
                        setError("No se pudieron cargar los eventos: Respuesta vacía")
                    }
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun fetchMisEventos() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                isError = false
                
                Log.d("MIS_EVENTOS", "Cargando mis eventos...")
                
                // Obtener el token de autenticación (deberías tener una forma de obtenerlo)
                val token = "Bearer ${SessionManager.getToken()}" // Asegúrate de tener un SessionManager
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMisEventos(token)
                }
                
                if (response.isSuccessful) {
                    val eventosResponse = response.body()
                    if (eventosResponse != null) {
                        misEventos = eventosResponse.eventos
                        Log.d("MIS_EVENTOS", "Mis eventos cargados: ${misEventos.size}")
                    } else {
                        setError("No se pudieron cargar tus eventos: Respuesta vacía")
                    }
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleFavorito(eventoId: Int) {
        viewModelScope.launch {
            try {
                Log.d("FAVORITOS", "Toggle favorito para evento $eventoId")
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.toggleFavorito(eventoId)
                }
                
                // Verificar el código de respuesta
                when (response.code()) {
                    200 -> {
                        // Actualizar el estado del evento en la lista local
                        eventos = eventos.map { evento ->
                            if (evento.id == eventoId) {
                                evento.copy(isFavorito = !evento.isFavorito)
                            } else {
                                evento
                            }
                        }
                        Log.d("FAVORITOS", "Favorito actualizado correctamente")
                    }
                    else -> handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    private fun handleErrorResponse(response: retrofit2.Response<*>) {
        val errorBody = response.errorBody()?.string()
        Log.e("EVENTOS", "Error: ${response.code()} - $errorBody")
        
        try {
            val errorResponse = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
            val message = errorResponse?.get("message") as? String ?: errorResponse?.get("error") as? String
            setError(message ?: "Error en la operación (${response.code()})")
        } catch (e: Exception) {
            setError("Error en la operación: ${response.message()} (${response.code()})")
        }
    }
    
    private fun handleException(e: Exception) {
        Log.e("EVENTOS", "Excepción: ${e.message}", e)
        setError("Error de conexión: ${e.message}")
    }
    
    // Función para establecer error
    private fun setError(error: String?) {
        isError = error != null
        errorMessage = error
        Log.e("EVENTOS", "ERROR: $error")
    }
    
    // Función para parsear la fecha
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            // Si hay un error al parsear, devolvemos una fecha muy lejana para que aparezca al final
            LocalDate.of(9999, 12, 31)
        }
    }
    
    // Función para parsear la hora
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) {
            // Si hay un error al parsear, devolvemos una hora muy tardía para que aparezca al final
            LocalTime.of(23, 59, 59)
        }
    }

    fun clearError() {
        isError = false
        errorMessage = null
    }
}